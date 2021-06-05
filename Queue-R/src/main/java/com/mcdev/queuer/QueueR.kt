package com.mcdev.queuer

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import java.io.IOException

class QueueR (activity: Activity){
    init {
        activity.setContentView(R.layout.activity_scan)
        create(activity)
    }

    fun create(activity: Activity) {
        val barcodeDetector = BarcodeDetector.Builder(activity)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()

        val cameraSource = CameraSource.Builder(activity, barcodeDetector)
            .setRequestedPreviewSize(1920, 1080)// this.getwidth()/ this.getHeight()
            .setAutoFocusEnabled(true)
            .build()

        val cam: CameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager


        val flashImgBtn: ImageButton = activity.findViewById(R.id.flash_image_button)
        flashImgBtn.setOnClickListener {
            Toast.makeText(activity.applicationContext, "Flash button clicked", Toast.LENGTH_LONG).show()
            var cameraId: String = cam.cameraIdList[0]
            cam.setTorchMode(cameraId, true)
        }


        val scanSurfaceView: SurfaceView = activity.findViewById(R.id.scan_surface_view)
        scanSurfaceView.holder.addCallback(object : SurfaceHolder.Callback{
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    if (ActivityCompat.checkSelfPermission(
                            activity, Manifest.permission.CAMERA) ==
                                    PackageManager.PERMISSION_GRANTED
                        ){

                        cameraSource.start(scanSurfaceView.holder)
                    }else{
//                        ActivityCompat.requestPermissions(this@ScanActivity,
//                            String[]{ Manifest.permission.CAMERA }, REQUEST_CAMERA_PERMISSION)
                    }
                } catch (e: IOException) {
//                    Log.d(TAG, "surfaceCreated: $e")
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
            }

        })
    }

    /**
    * Check if flash is supported
    **/
    fun isFlashAvailable(activity: Activity): Boolean {
        return activity.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }
}