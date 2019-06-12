package com.example.drawkanji.utils

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Mat

fun Mat.convertToBitmap(bitmapConfig: Bitmap.Config): Bitmap {
    val bitmap = Bitmap.createBitmap(cols(), rows(), bitmapConfig)
    // convert mat to bitmap
    Utils.matToBitmap(this, bitmap)
    return bitmap
}