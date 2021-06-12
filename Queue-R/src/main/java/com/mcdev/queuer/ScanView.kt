package com.mcdev.queuer


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.camera2.*
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.util.isNotEmpty
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.mcdev.queuer.databinding.ScanViewBinding
import java.lang.reflect.Field


class ScanView(
    context: Context,
    attrs: AttributeSet? = null,
): ConstraintLayout (context, attrs) {

    private val TAG = "ScanView"
    private val layoutInflater = LayoutInflater.from(context)
    private val binding = ScanViewBinding.inflate(layoutInflater, this, true)

    private lateinit var surfaceHolder: SurfaceHolder
    private var isFlashEnabled: Boolean = false

    init {
        initScanView(context)

        /*Gallery Button*/
        binding.galleryImageButton.setOnClickListener { /*TODO not yet implemented*/ }

    }


    /**
    *Initialize scan view
     *  */
    fun initScanView(context: Context) {

        val detector = initBarcodeDetector(context, Barcode.QR_CODE)

        detector.setProcessor(object : Detector.Processor<Barcode>{
            override fun release() {

            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                if (detections.detectedItems.isNotEmpty()) {
                    val barcode = detections.detectedItems
                    if (barcode?.size() ?: 0 > 0) {
                        // show barcode content value
                        Log.d(TAG, "receiveDetections: " + barcode?.valueAt(0)?.displayValue)
//                        Toast.makeText(context,  barcode?.valueAt(0)?.displayValue ?: "", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        })


        val cameraSource = CameraSource.Builder(context,detector)
            .setRequestedFps(25f)
            .setAutoFocusEnabled(true).build()


        binding.scanSurfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                // check camera permission for api version 23
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    cameraSource.start(holder)

                    binding.flashImageButton.setOnClickListener {
                        if (isFlashEnabled) {
                            isFlashEnabled = false
                            flashOnButton(isFlashEnabled, cameraSource)
                        } else {
                            isFlashEnabled = true
                            flashOnButton(isFlashEnabled, cameraSource)
                        }
                    }

                }
                else Toast.makeText(context, "Get permissions", Toast.LENGTH_LONG).show()
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {

            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()            }
        })
    }
    private fun getCamera(cameraSource: CameraSource): Camera? {
        val declaredFields: Array<Field> = CameraSource::class.java.declaredFields
        for (field in declaredFields) {
            if (field.type === Camera::class.java) {
                field.isAccessible = true
                try {
                    val camera = field.get(cameraSource) as Camera
                    return if (camera != null) {
                        camera
                    } else null
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


    fun surfaceView() {



        binding.scanSurfaceView.holder.addCallback(object: SurfaceHolder.Callback{
            override fun surfaceCreated(holder: SurfaceHolder) {
//                startCameraPreview2(false)

            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                startCameraPreview( width, height,false)
                }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
            }
        })

    }

    fun startCameraPreview(width: Int, height: Int, enableFlash: Boolean) {
        try {
            // TODO
            val cameraBkgHandler = Handler()

            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

            cameraManager.cameraIdList.find {
                val characteristics = cameraManager.getCameraCharacteristics(it)
                val cameraDirection = characteristics.get(CameraCharacteristics.LENS_FACING)

                return@find cameraDirection != null && cameraDirection == CameraCharacteristics.LENS_FACING_BACK
            }?.let {
                val cameraStateCallback = object : CameraDevice.StateCallback() {
                    override fun onOpened(camera: CameraDevice) {

                        val barcodeDetector = BarcodeDetector.Builder(context)
                            .setBarcodeFormats(Barcode.QR_CODE)
                            .build()

                        /*when barcode is not functional*/
                        if (!barcodeDetector.isOperational) {
                            Toast.makeText(context, "Barcode not working", Toast.LENGTH_LONG).show()
                        }

                        barcodeDetector.setProcessor(object: Detector.Processor<Barcode>{
                            override fun release() {
                                TODO("Not yet implemented")
                            }

                            override fun receiveDetections(p0: Detector.Detections<Barcode>) {

                                var barcodes = p0.detectedItems
                                if (barcodes.size() != 0) {
                                    Toast.makeText(context, " working", Toast.LENGTH_LONG).show()
                                }
                            }

                        })

                        val cameraSource = CameraSource.Builder(context, barcodeDetector)
                            .setRequestedFps(25f)
                            .setAutoFocusEnabled(true).build()

//                        binding.scanSurfaceView.holder.addCallback(surfaceCallBack)

//                        val imageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)
//                        imageReader.setOnImageAvailableListener({reader ->
//                            val cameraImage = reader.acquireNextImage()
//
//                            val buffer = cameraImage.planes.first().buffer
//                            val bytes = ByteArray(buffer.capacity())
//                            buffer.get(bytes)
//
//                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0 , bytes.count(), null)
//                            val frameToProcess = Frame.Builder().setBitmap(bitmap).build()
//                            val barcodeResults = barcodeDetector.detect(frameToProcess)
//
//                            if (barcodeResults.size() > 0) {
//                                Log.d(TAG, "onOpened: Barcode Detected!")
//                                Toast.makeText(context, "Barcode detected", Toast.LENGTH_LONG)
//                                    .show()
//                            } else {
//                                Log.d(TAG, "onOpened: No barcode detected!")
//                            }
//
//                            cameraImage.close()
//                        }, cameraBkgHandler)


                        val captureStateCallback = object : CameraCaptureSession.StateCallback() {
                            override fun onConfigured(session: CameraCaptureSession) {
                                var builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                                builder.addTarget(binding.scanSurfaceView.holder.surface)

                                if (enableFlash) { builder = enableFlashMode(builder, enableFlash) }

                                session.setRepeatingRequest(builder.build(), null, null)

                                val request = builder.build()
                                session.capture(request, null, null)
                            }

                            override fun onConfigureFailed(session: CameraCaptureSession) {
                                // TODO
                                Log.d(TAG, "onConfigureFailed")
                            }
                        }

                        camera.createCaptureSession(
                            listOf(binding.scanSurfaceView.holder.surface),
                            captureStateCallback,
                            cameraBkgHandler
                        )
                    }

                    override fun onClosed(camera: CameraDevice) {
                        // TODO
                        Log.d(TAG, "onClosed")
                    }

                    override fun onDisconnected(camera: CameraDevice) {
                        // TODO
                        Log.d(TAG, "onDisconnected")
                    }

                    override fun onError(camera: CameraDevice, error: Int) {
                        // TODO
                        Log.d(TAG, "onError")
                    }
                }

                cameraManager.openCamera(it, cameraStateCallback, cameraBkgHandler)
                return
            }

            // TODO: - No available camera found case

        } catch (e: CameraAccessException) {
            // TODO
            Log.e(TAG, e.message!!)
        } catch (e: SecurityException) {
            // TODO
            Log.e(TAG, e.message!!)
        }
    }

    fun initBarcodeDetector(context: Context, barcodeType: Int): BarcodeDetector {
        return BarcodeDetector.Builder(context)
            .setBarcodeFormats(barcodeType)
            .build()
    }

    fun setCameraSource(cameraSource: CameraSource, detector: BarcodeDetector): CameraSource{
        return CameraSource.Builder(context, detector)
            .setRequestedFps(25f)
            .setAutoFocusEnabled(true).build()
    }


    /*Enables Flash light*/
    fun enableFlashMode(builder: CaptureRequest.Builder, mode: Boolean): CaptureRequest.Builder {
        builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH)
        builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
        return builder
    }
}