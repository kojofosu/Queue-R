package com.mcdev.queuer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.mcdev.queuer.databinding.ActivityScanBinding
import java.io.IOException


class ScanActivity : AppCompatActivity() {
    private val TAG = "ScanActivity"
    private lateinit var binding: ActivityScanBinding
    private val REQUEST_CAMERA_PERMISSION: Int = 200
    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        barcodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()

        cameraSource = CameraSource.Builder(this, barcodeDetector)
            .setRequestedPreviewSize(1920, 1080)
            .setAutoFocusEnabled(true)
            .build()

        binding.scanSurfaceView.holder.addCallback(object : SurfaceHolder.Callback{
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    cameraSource.start(binding.scanSurfaceView.holder)
//                    if (ActivityCompat.checkSelfPermission(
//                            this@ScanActivity, Manifest.permission.CAMERA) ==
//                                    PackageManager.PERMISSION_GRANTED
//                        ){
//                        cameraSource.start(binding.scanSurfaceView.holder)
//                    }else{
////                        ActivityCompat.requestPermissions(this@ScanActivity,
////                            String[]{ Manifest.permission.CAMERA }, REQUEST_CAMERA_PERMISSION)
//                    }
                } catch (e: IOException) {
                    Log.d(TAG, "surfaceCreated: $e")
                }

            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                TODO("Not yet implemented")
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }

        })
    }
}