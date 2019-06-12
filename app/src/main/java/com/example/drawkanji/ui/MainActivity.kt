package com.example.drawkanji.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import com.example.drawkanji.R
import com.example.drawkanji.models.ImagesDifferenceData
import com.example.drawkanji.utils.calculateImagesDifference
import com.example.drawkanji.utils.convertToBitmap
import com.example.drawkanji.utils.getPath
import kotlinx.android.synthetic.main.activity_main.*
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs

class MainActivity : Activity() {

    private var img1Mat: Mat? = null
    private var img2Mat: Mat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        OpenCVLoader.initDebug()
        setFirstImgBtn.setOnClickListener {
            openGallery(RC_FIRST_IMG)
        }
        setSecondImgBtn.setOnClickListener {
            openGallery(RC_SECOND_IMG)
        }
        subtractImagesBtn.setOnClickListener {
            if (areImagesPicked()) {
                val resultingMat = subtractImages(img2Mat!!, img1Mat!!)
                resultingImg.setImageBitmap(resultingMat.convertToBitmap(Bitmap.Config.RGB_565))
                val imagesDifferenceData = calculateImagesDifference(img1Mat!!, img2Mat!!)
                showImagesDifferenceDataOnLayout(imagesDifferenceData)
            } else {
                showImagesNotPickedError()
            }
        }
        huMomentsWrapper.setOnClickListener {
            huMomentsWrapper.visibility = View.GONE
        }
        drawBtn.setOnClickListener {
            startActivity(Intent(this, DrawActivity::class.java))
        }
    }

    private fun subtractImages(img1: Mat, img2: Mat): Mat {
        val resultingMat = Mat()
        Core.subtract(img1, img2, resultingMat)
        return resultingMat
    }

    private fun areImagesPicked() = img1Mat != null && img2Mat != null

    private fun showImagesNotPickedError() {
        Toast.makeText(applicationContext, "You must pick both images", Toast.LENGTH_LONG).show()
    }

    private fun openGallery(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == RC_FIRST_IMG || requestCode == RC_SECOND_IMG) && resultCode == RESULT_OK && data != null) {
            val imageUri = data.data
            // get path from URI
            val path = getPath(this, imageUri)
            displayImage(path, requestCode)
        }
    }

    private fun showImagesDifferenceDataOnLayout(imagesDifferenceData: ImagesDifferenceData) {
        huMoments1.text = imagesDifferenceData.huMomentsValues1.toString()
        huMoments2.text = imagesDifferenceData.huMomentsValues2.toString()
        shapeDistanceText.text = imagesDifferenceData.shapeDistance.toString()
        huMomentsWrapper.visibility = View.VISIBLE
    }

    private fun displayImage(path: String?, requestCode: Int) {
        if (path == null) {
            return
        }

        val mat = Imgcodecs.imread(path, Imgcodecs.IMREAD_REDUCED_GRAYSCALE_2)

        if (requestCode == RC_FIRST_IMG) {
            img1.setImageBitmap(mat.convertToBitmap(Bitmap.Config.RGB_565))
            img1Mat = mat
        } else if (requestCode == RC_SECOND_IMG) {
            img2.setImageBitmap(mat.convertToBitmap(Bitmap.Config.RGB_565))
            img2Mat = mat
        }
    }

    companion object {
        const val RC_FIRST_IMG = 101
        const val RC_SECOND_IMG = 102
    }
}