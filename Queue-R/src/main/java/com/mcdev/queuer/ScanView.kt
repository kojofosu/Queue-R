package com.mcdev.queuer


import android.content.Context
import android.graphics.ImageFormat
import android.hardware.Camera
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.mcdev.queuer.databinding.ScanViewBinding
import java.lang.Compiler.enable


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
        binding.galleryImageButton.setOnClickListener { /*TODO not yet implemented*/ }
        binding.flashImageButton.setOnClickListener {
            if (isFlashEnabled) {
                isFlashEnabled = false
                startCameraPreview(isFlashEnabled)
            } else {
                isFlashEnabled = true
                startCameraPreview(isFlashEnabled)
            }
        }
        camera()
    }

    fun camera() {
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
//                startCameraPreview(1920, 1080, false)
                }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
            }
        })

    }

    private fun startCameraPreview(enableFlash: Boolean) {
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
//                            listOf(surfaceHolder.surface),
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


    /*Enables Flash light*/
    fun enableFlashMode(builder: CaptureRequest.Builder, mode: Boolean): CaptureRequest.Builder {
        builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH)
        builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
        return builder
    }
}