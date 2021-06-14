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
import com.google.android.gms.vision.barcode.Barcode
import com.mcdev.queuer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(){

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        val getContent = registerForActivityResult(ActivityResultContracts.GetContent()){ uri: Uri? ->
//            val result = binding.scanView.decode(uri!!)
            val bitmp = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            val result: String = binding.scanView.decode(bitmp).displayValue

                val intent = Intent(applicationContext, GetCodeActivity::class.java)
                intent.putExtra("one", result)
                startActivity(intent)

        }

        val imgbtn = binding.scanView.getGalleryButton()
        imgbtn.setOnClickListener {
            Toast.makeText(applicationContext, "hey", Toast.LENGTH_LONG).show()
            intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
//            startActivityForResult(Intent.createChooser(intent, "Select Pic"), 111)


            getContent.launch("image/*")
        }


        binding.scanView.setQueueRListener(object: QueueRListener{
            override fun onRetrieved(barcode: Barcode) {
                var intent = Intent(applicationContext, GetCodeActivity::class.java)
                intent.putExtra("one", barcode.displayValue)
                startActivity(intent)
            }
        })

    }

    override fun onStart() {
        super.onStart()
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), Companion.REQUEST_CAMERA)
        } else {

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
//                binding.scanView.testSurface()
        } else {
//            NavHostFragment.findNavController(this).navigateUp()
        }
    }

    companion object {
        const val REQUEST_CAMERA = 1729
    }

}