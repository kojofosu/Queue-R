package com.mcdev.queuer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.mcdev.queuer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

//        binding.scanView.initBarcodeDetector()

    }

    override fun onStart() {
        super.onStart()
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), Companion.REQUEST_CAMERA)
        } else {
//            binding.scanView.testSurface()
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