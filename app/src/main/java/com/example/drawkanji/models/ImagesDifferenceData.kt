package com.example.drawkanji.models

data class ImagesDifferenceData(
    val huMomentsValues1: List<Double>,
    val huMomentsValues2: List<Double>,
    val shapeDistance: Double
)