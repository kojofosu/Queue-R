package com.mcdev.queuer


import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.mcdev.queuer.databinding.ScanViewBinding


class ScanView(context: Context,
               attrs: AttributeSet? = null,
    ): ConstraintLayout (context, attrs) {

    private val TAG = "ScanView"
    private val layoutInflater = LayoutInflater.from(context)
    private val binding = ScanViewBinding.inflate(layoutInflater, this, true)


    init {
        binding.galleryImageButton.setOnClickListener { Toast.makeText(context, "First yu", Toast.LENGTH_LONG).show() }
    }

}