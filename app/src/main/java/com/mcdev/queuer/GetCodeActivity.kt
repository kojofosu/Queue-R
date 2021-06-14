package com.mcdev.queuer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

//import com.mcdev.queuer.databinding

class GetCodeActivity : AppCompatActivity() {

//    private lateinit var binding: Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_code)

    val etextV = findViewById<TextView>(R.id.content_tv)
    var barcodeValue :String
    if (intent != null) {
        barcodeValue = intent.extras!!.getString("one", "")
        etextV.text = barcodeValue
    }



    }
}