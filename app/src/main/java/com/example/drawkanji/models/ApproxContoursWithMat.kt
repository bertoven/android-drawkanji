package com.example.drawkanji.models

import org.opencv.core.Mat

data class ApproxContoursWithMat(
    val contours: MutableList<DoubleArray>,
    val mat: Mat
)