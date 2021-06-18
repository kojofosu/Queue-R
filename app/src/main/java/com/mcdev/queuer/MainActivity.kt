package com.mcdev.queuer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.mcdev.queuer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(){

    private lateinit var binding: ActivityMainBinding
    private val REQUEST_CAMERA = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val detector: BarcodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.QR_CODE)
            .build()

        val cameraSource =  CameraSource.Builder(this,detector)
            .setRequestedFps(25f)
            .setAutoFocusEnabled(true).build()


//        binding.scanView.setBarcodeDetector(detector)
        binding.scanView.setCameraSource(cameraSource, detector)
        binding.scanView.setFlashIconOverlay(true)


        binding.scanView.initGalleryButton(this@MainActivity.activityResultRegistry)

//        val getContent = registerForActivityResult(ActivityResultContracts.GetContent()){ uri: Uri? ->
//            val result = binding.scanView.decode(uri!!)
//
//            val intent = Intent(applicationContext, GetCodeActivity::class.java)
//            intent.putExtra("one", result)
//            startActivity(intent)
//
//        }
//
//        val imgbtn = binding.scanView.getGalleryButton()
//        imgbtn.setOnClickListener {
//            intent = Intent()
//            intent.type = "image/*"
//            intent.action = Intent.ACTION_GET_CONTENT
//
//            getContent.launch("image/*")
//        }



        binding.scanView.setQueueRListener(object: QueueRListener{
            override fun onRetrieved(barcode: Barcode) {
                val intent = Intent(applicationContext, GetCodeActivity::class.java)
                intent.putExtra("one", barcode.displayValue)
                startActivity(intent)
            }
        })

    }

    override fun onStart() {
        super.onStart()
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA)
        } else {
            binding.scanView.startScan()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            && requestCode == REQUEST_CAMERA) {
//                binding.scanView.startScan()
        }
    }


}