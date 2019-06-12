package com.example.drawkanji.utils

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Mat

fun Bitmap.convertToMat(bitmapConfig: Bitmap.Config): Mat {
    val mat = Mat()
    val bmp32 = copy(bitmapConfig, true)
    Utils.bitmapToMat(bmp32, mat)
    return mat
}