package com.mcdev.queuer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.FileDescriptor
import java.io.IOException

object Utils {

    const val IMAGE_MIME_TYPE = "image/*"
        /**
         * Opens a Bitmap file given its URI
         * */
        @Throws(IOException::class)
        fun getBitmapFromUri(context: Context,uri: Uri): Bitmap{
            val parcelFileDescriptor: ParcelFileDescriptor? =
                context.contentResolver.openFileDescriptor(uri, "r")

            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
            return image
        }


}