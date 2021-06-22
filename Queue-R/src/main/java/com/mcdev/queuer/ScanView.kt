package com.mcdev.queuer


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.widget.ImageButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.isNotEmpty
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.mcdev.queuer.databinding.ScanViewBinding
import java.lang.reflect.Field


class ScanView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): ConstraintLayout (context, attrs, defStyleAttr) {

    private val TAG = "ScanView"
    private val layoutInflater = LayoutInflater.from(context)
    private val binding = ScanViewBinding.inflate(layoutInflater, this, true)

    private lateinit var surfaceHolder: SurfaceHolder
    var isFlashEnabled: Boolean = false
    private var listener: QueueRListener? = null
    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource

    private val OPEN_GALLERY = "OPEN_GALLERY"
    private lateinit var registry: ActivityResultRegistry

    init {
        //load style attributes
        val attributes = context.theme.obtainStyledAttributes(attrs, R.styleable.ScanView, defStyleAttr, 0)

        val flashIconOverlay = attributes.getBoolean(R.styleable.ScanView_setFlashIconOverlay, true)

        setFlashIconOverlay(flashIconOverlay)

        binding.galleryImageButton.setOnClickListener {
            initGalleryButton(registry).launch(Utils.IMAGE_MIME_TYPE)
        }
    }

    /**
     * Initializes the gallery button to allow image selection in gallery
     * @param registry ActivityResultRegistry
    * */
    private fun initGalleryButton(registry: ActivityResultRegistry): ActivityResultLauncher<String> {
        return registry.register(OPEN_GALLERY , ActivityResultContracts.GetContent()){ uri: Uri? ->
            val bitmap = Utils.getBitmapFromUri(context, uri!!)
            val barcode = decode(bitmap)

            Log.d(TAG, "initGalleryButton: $barcode")
            this.listener?.onRetrieved(barcode)
        }
    }



    /**
    * Sets visibility of the flash icon overlay over the scan view
     * @param mode boolean
    **/
    @Suppress("MemberVisibilityCanBePrivate")
    fun setFlashIconOverlay(mode: Boolean) {
        when (mode) {
            true -> binding.flashAnimationView.visibility = VISIBLE
            else -> binding.flashAnimationView.visibility = GONE
        }
    }

    /**
     * Initializes the scanner
     * @param barcodeDetector
     * @param cameraSource
     * @param activityResultRegistry
     * */
    fun initialize(barcodeDetector: BarcodeDetector,
                   cameraSource: CameraSource,
                   activityResultRegistry: ActivityResultRegistry) {
        this.barcodeDetector = barcodeDetector
        this.cameraSource = cameraSource
        this.registry = activityResultRegistry
    }

    /**
    *Initialize scan view and starts scan
     * @param barcodeDetector BarcodeDetector
     * @param cameraSource CameraSource
     *  */
    @RequiresPermission("android.permission.CAMERA", conditional = false)
    fun startScan() {
        //initialize gallery button
        initGalleryButton(registry)
        barcodeDetector.setProcessor(object : Detector.Processor<Barcode>{
            override fun release() {

            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                if (detections.detectedItems.isNotEmpty()) {
                    val barcode = detections.detectedItems
                    if (barcode?.size() ?: 0 > 0) {
                        // show barcode content value
                        listener?.onRetrieved(barcode.valueAt(0))
                        Log.d(TAG, "receiveDetections: " + barcode?.valueAt(0)?.displayValue)
                        cameraSource.stop()
                        cameraSource.release()//todo
                    }
                }
            }

        })



        binding.scanSurfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(holder: SurfaceHolder) {
                cameraSource.start(holder)

                binding.flashAnimationView.frame = 60
                binding.flashImageButton.setOnClickListener {
                    if (isFlashEnabled) {
                        isFlashEnabled = false
                        playFlashAnimation(150, 180)
                        flashOnButton(isFlashEnabled, cameraSource)
                    } else {
                        isFlashEnabled = true
                        playFlashAnimation(60, 90)
                        flashOnButton(isFlashEnabled, cameraSource)
                    }
                }

            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {

            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
                cameraSource.release()
            }
        })
    }

    private fun playFlashAnimation(startFrame: Int, endFrame: Int) {
        binding.flashAnimationView.setMinAndMaxFrame(startFrame, endFrame)
        binding.flashAnimationView.playAnimation()
    }

    private fun getCamera(cameraSource: CameraSource): Camera? {
        val declaredFields: Array<Field> = CameraSource::class.java.declaredFields
        for (field in declaredFields) {
            if (field.type === Camera::class.java) {
                field.isAccessible = true
                try {
                    return field.get(cameraSource) as Camera
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
                break
            }
        }
        return null
    }

    private fun flashOnButton(flashMode: Boolean, mCameraSource: CameraSource) {
        val camera = getCamera(mCameraSource)
        if (camera != null) {
            try {
                val param: Camera.Parameters = camera.parameters
                param.flashMode =
                    if (flashMode) Camera.Parameters.FLASH_MODE_TORCH else Camera.Parameters.FLASH_MODE_OFF
                camera.parameters = param
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Sets and initialises the barcode detector
     * @param barcodeDetector BarcodeDetector
     * @return BarcodeDetector
    * */
    fun setBarcodeDetector(barcodeDetector: BarcodeDetector): BarcodeDetector {
        this.barcodeDetector = barcodeDetector
        return barcodeDetector
    }

    /**
     * Sets and initializes the barcode detector
     * @param barcodeType Int : This specifies the barcode type e.g Barcode.QR_CODE
     * @return BarcodeDetector
    * */
    fun setBarcodeDetector(barcodeType: Int): BarcodeDetector {
        val barcodeDetector = BarcodeDetector.Builder(context)
            .setBarcodeFormats(barcodeType)
            .build()

        this.barcodeDetector = barcodeDetector
        return barcodeDetector
    }

    /**
     * Sets and initializes the camera source
     * @param cameraSource camera source
     * @param detector barcode detector
     * @return CameraSource
    * */
    fun setCameraSource(cameraSource: CameraSource, detector: BarcodeDetector): CameraSource{
        this.cameraSource = cameraSource
        this.barcodeDetector = detector
        return cameraSource
    }

    fun setCameraSource(cameraSource: CameraSource): CameraSource{
        this.cameraSource = cameraSource
        return cameraSource
    }


    /**
     * Sets listener for code scanning
     * @param listener QueueRListener
    * */
    fun setQueueRListener(listener: QueueRListener) {
        this.listener = listener
    }

    fun setActivityResultRegistry(activityResultRegistry: ActivityResultRegistry) {
        this.registry = activityResultRegistry
    }

    /**
     * Gets the gallery image button for more control and customizatoin
     * @return ImageButton
    * */
    fun getGalleryButton(): ImageButton {
        return binding.galleryImageButton
    }

    /**
     * Decodes barcode
     * @param uri Uri
     * @return The scanned barcodes' display value
    * */
    fun decode(uri: Uri): String? {
        var result: String? = null
        val inputStream  = context.contentResolver.openInputStream(uri)
        var bitmap = BitmapFactory.decodeStream(inputStream)

        if (bitmap == null) {
            Log.e(TAG, "scanUri: Invalid bitmap uri : ${uri.toString()}")
        }

        val width = bitmap.width
        val height = bitmap.height

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        bitmap.recycle()
        bitmap = null //todo investigate

        val rgbLuminanceSource = RGBLuminanceSource(width, height, pixels)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(rgbLuminanceSource))
        val multiFormatReader = MultiFormatReader()
        try {
            val decodedResult = multiFormatReader.decode(binaryBitmap)
            result = decodedResult.text
            Log.d(TAG, "scanUri: QR image content : $result")
        } catch (e: NotFoundException) {
            Log.e(TAG, "scanUri: $e", )
        }
        return result
    }

    //todo create a decode function that returns the barcode object with uri parameter

    /**
     * Decodes barcode
     * @param bitmap Bitmap of selected image
     * @param barcodeType Barcode type e.g Barcode.QR_CODE
     * @return Barcode
    * */
    fun decode(bitmap: Bitmap, barcodeType: Int): Barcode {
        //Setup the barcode detector
        val barcodeDetector = setBarcodeDetector(barcodeType)

        if (!barcodeDetector.isOperational) {
            Log.e(TAG, "decode: barcode is not functional" )
        }

        //Detect the barcode
        val frame = Frame.Builder().setBitmap(bitmap).build()
        val barcodes = barcodeDetector.detect(frame)

        //Decode the barcode
        return barcodes.valueAt(0)
    }

    /**
     * Decodes barcode
     * @param bitmap Bitmap of selected image
     * @return Barcode
     * */
    fun decode(bitmap: Bitmap): Barcode {
        //Setup the barcode detector
        val barcodeDetector = setBarcodeDetector(Barcode.QR_CODE)

        if (!barcodeDetector.isOperational) {
            Log.e(TAG, "decode: barcode is not functional" )
        }

        //Detect the barcode
        val frame = Frame.Builder().setBitmap(bitmap).build()
        val barcodes = barcodeDetector.detect(frame)

        //Decode the barcode
        return barcodes.valueAt(0)
    }
}