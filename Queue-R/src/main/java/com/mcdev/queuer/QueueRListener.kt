package com.mcdev.queuer

interface QueueRListener {
    fun onRetrieved(barcode: com.google.android.gms.vision.barcode.Barcode)

    fun onFailed(message: String)
}