package com.example.drawkanji.utils

import com.example.drawkanji.models.ImagesDifferenceData
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

fun calculateImagesDifference(img1Mat: Mat, img2Mat: Mat): ImagesDifferenceData {
    val moments1 = Imgproc.moments(img1Mat)
    val moments2 = Imgproc.moments(img2Mat)
    val huMat1 = Mat()
    val huMat2 = Mat()
    Imgproc.HuMoments(moments1, huMat1)
    Imgproc.HuMoments(moments2, huMat2)
    val huMomentsValues1 = mutableListOf<Double>()
    val huMomentsValues2 = mutableListOf<Double>()
    for (i in 0 until 7) {
        val value1 = huMat1.get(i, 0)[0]
        val value2 = huMat2.get(i, 0)[0]
        huMomentsValues1.add(-1 * Math.copySign(1.0, value1) * Math.log10(Math.abs(value1)))
        huMomentsValues2.add(-1 * Math.copySign(1.0, value2) * Math.log10(Math.abs(value2)))
    }
    val shapeDistance = Imgproc.matchShapes(img1Mat, img2Mat, Imgproc.CONTOURS_MATCH_I2, 0.0)
    return ImagesDifferenceData(huMomentsValues1, huMomentsValues2, shapeDistance)
}