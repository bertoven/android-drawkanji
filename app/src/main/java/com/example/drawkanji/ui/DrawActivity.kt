package com.example.drawkanji.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.drawkanji.R
import com.example.drawkanji.models.ApproxContoursWithMat
import com.example.drawkanji.utils.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_draw.*
import org.opencv.core.*
import org.opencv.core.CvType.CV_8UC1
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc

class DrawActivity : Activity(), DrawView.OnDrawLineCompletedListener {

    private var imageContours: MutableList<DoubleArray> = mutableListOf()
    private var drawingContours: MutableList<DoubleArray> = mutableListOf()

    private val kanjiStrokes: MutableList<Mat> = mutableListOf()
    private var originalImagesMats: MutableList<Mat> = mutableListOf()

    private var currentKanjiStroke: Mat? = null
    private var currentOriginalImageMat: Mat? = null
    private var currentStep = 0

    private val kanjiStrokesAdapter: KanjiStrokesAdapter by lazy { KanjiStrokesAdapter() }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draw)

        drawView.setOnDrawLineCompletedListener(this)
        pickImageBtn.setOnClickListener {
            openGallery()
        }
        drawView.setOnTouchListener { v, _ ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            return@setOnTouchListener false
        }
        resetBtn.setOnClickListener { reset() }
    }

    override fun onDestroy() {
        drawView.setOnDrawLineCompletedListener(null)
        super.onDestroy()
    }

    override fun onDrawLineCompleted(bitmap: Bitmap) {
        imageFromCanvas.setImageBitmap(bitmap)

        val mat = bitmap.convertToMat(Bitmap.Config.ARGB_8888)
        val resizedMat = Mat()
        Imgproc.resize(mat, resizedMat, Size(Point(310.0, 310.0)))
        val grayMat = Mat()
        Imgproc.cvtColor(resizedMat, grayMat, Imgproc.COLOR_BGR2GRAY)

        val drawingContours = mutableListOf<MatOfPoint>()
        Imgproc.findContours(
            grayMat,
            drawingContours,
            Mat(),
            Imgproc.RETR_TREE,
            Imgproc.CHAIN_APPROX_NONE
        )

        val drawingMatAndContours = getApproxContoursWithMat(drawingContours, grayMat.size())
        imageFromCanvasContours.setImageBitmap(drawingMatAndContours.mat.convertToBitmap(Bitmap.Config.RGB_565))
        this.drawingContours = drawingMatAndContours.contours

        if (currentKanjiStroke != null) {
            pickedImage.setImageBitmap(currentKanjiStroke!!.convertToBitmap(Bitmap.Config.RGB_565))

            val imageContours = mutableListOf<MatOfPoint>()
            Imgproc.findContours(
                currentKanjiStroke,
                imageContours,
                Mat(),
                Imgproc.RETR_TREE,
                Imgproc.CHAIN_APPROX_NONE
            )

            val imageMatAndContours =
                getApproxContoursWithMat(imageContours, currentKanjiStroke!!.size())
            pickedImageContours.setImageBitmap(imageMatAndContours.mat.convertToBitmap(Bitmap.Config.RGB_565))
            this.imageContours = imageMatAndContours.contours
            compareImagesContours()
        } else {
            snackbar(
                drawView,
                "You must pick image to get contours",
                Snackbar.LENGTH_LONG,
                SnackbarType.ERROR
            )
        }
    }

    private fun compareImagesContours() {
        if (imageContours.isEmpty() || drawingContours.isEmpty()) {
            snackbar(drawView, "Empty contour list", Snackbar.LENGTH_LONG, SnackbarType.ERROR)
            drawView.setPreviousBackground()
        } else {
            val threshold = 30
            val setOfIndices = mutableSetOf<Int>()
            var isContourCorrect: Boolean

            for (imageContour in imageContours) {
                isContourCorrect = false
                for (i in drawingContours.indices) {
                    if (Math.abs(imageContour[0] - drawingContours[i][0]) < threshold &&
                        Math.abs(imageContour[1] - drawingContours[i][1]) < threshold
                    ) {
                        isContourCorrect = true
                        setOfIndices.add(i)
                        break
                    }
                }
                if (!isContourCorrect) {
                    onImagesNotSimilar()
                    return
                }
            }

            val remainingIndices = drawingContours.indices - setOfIndices
            for (i in remainingIndices) {
                isContourCorrect = false
                for (imageContour in imageContours) {
                    if (Math.abs(imageContour[0] - drawingContours[i][0]) < threshold &&
                        Math.abs(imageContour[1] - drawingContours[i][1]) < threshold
                    ) {
                        isContourCorrect = true
                        break
                    }
                }
                if (!isContourCorrect) {
                    onImagesNotSimilar()
                    return
                }
            }
            goToNextStep()
            setOriginalImageBackground()
        }
    }

    private fun onImagesNotSimilar() {
        snackbar(drawView, "Images not similar", Snackbar.LENGTH_LONG, SnackbarType.ERROR)
        drawView.setPreviousBackground()
    }

    private fun goToNextStep() {
        if (currentStep >= kanjiStrokes.size - 1) {
            currentOriginalImageMat = originalImagesMats[currentStep]
            snackbar(drawView, "Kanji drawn correctly", Snackbar.LENGTH_LONG)
        } else {
            currentStep++
            currentKanjiStroke = kanjiStrokes[currentStep]
            currentOriginalImageMat = originalImagesMats[currentStep - 1]
        }
    }

    private fun reset() {
        drawView.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
        currentStep = 0
        currentKanjiStroke = kanjiStrokes[0]
        currentOriginalImageMat = originalImagesMats[0]
    }

    private fun setOriginalImageBackground() {
        if (currentOriginalImageMat != null) {
            val bitmapDrawable =
                BitmapDrawable(
                    resources,
                    currentOriginalImageMat!!.convertToBitmap(Bitmap.Config.RGB_565)
                )
            drawView.background = bitmapDrawable
        }
    }

    private fun getApproxContoursWithMat(
        contours: List<MatOfPoint>,
        matSize: Size
    ): ApproxContoursWithMat {

        val matContour = Mat(matSize, CV_8UC1)
        val contoursList = mutableListOf<DoubleArray>()

        for (contour in contours) {
            for (i in 0 until contour.rows() step 10) {
                val contourDoubleArray = contour.get(i, 0)
                contoursList.add(contourDoubleArray)
                val row = contourDoubleArray[1].toInt()
                val col = contourDoubleArray[0].toInt()
                val data = matContour.get(row, col)
                data[0] = 255.0
                matContour.put(row, col, data[0])
            }
        }

        return ApproxContoursWithMat(contoursList, matContour)
    }

    private fun openGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            RC_PICK_IMG
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_PICK_IMG && resultCode == RESULT_OK) {
            if (data != null && data.clipData != null) {
                val clipData = data.clipData!!
                val imagesPaths = mutableListOf<String>()
                for (i in 0 until clipData.itemCount) {
                    val path = getPath(this, clipData.getItemAt(i).uri)
                    if (path != null) {
                        imagesPaths.add(path)
                    } else {
                        snackbar(
                            drawLayout,
                            "Couldn't get paths of all of the images",
                            Snackbar.LENGTH_LONG,
                            SnackbarType.ERROR
                        )
                        return
                    }
                }
                imagesPaths.sortBy { it.substring(it.lastIndexOf("/")) }
                subtractImages(imagesPaths)
            } else {
                snackbar(
                    drawLayout,
                    "Error occurred while picking images from memory",
                    Snackbar.LENGTH_LONG,
                    SnackbarType.ERROR
                )
            }
        }
    }

    private fun subtractImages(imagesPaths: List<String>) {
        val kanjiStrokesBitmaps = mutableListOf<Bitmap>()
        kanjiStrokes.clear()
        originalImagesMats.clear()

        var previousImg = Mat()

        val kernelSize = Size(5.0, 5.0)
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, kernelSize)
        val anchor = Point(-1.0, -1.0)
        val iterations = 6

        for (i in imagesPaths.indices) {
            val img = Imgcodecs.imread(imagesPaths[i], Imgcodecs.IMREAD_GRAYSCALE)
            originalImagesMats.add(img)

            kanjiStrokesBitmaps += if (i == 0) {
                kanjiStrokes.add(img)
                previousImg = img
                img.convertToBitmap(Bitmap.Config.RGB_565)
            } else {
                val resultingMat = Mat()
                Core.subtract(img, previousImg, resultingMat)
                previousImg = img
                val dst = Mat()
                Imgproc.morphologyEx(resultingMat, dst, Imgproc.MORPH_OPEN, kernel, anchor)
//                Imgproc.morphologyEx(dst, dst, Imgproc.MORPH_CLOSE, kernel, anchor, iterations)
                kanjiStrokes.add(dst)
                dst.convertToBitmap(Bitmap.Config.RGB_565)
            }
        }
        initializeAdapter(kanjiStrokesBitmaps)
        reset()
        resetBtn.isEnabled = true
    }

    private fun initializeAdapter(kanjiStrokesBitmaps: List<Bitmap>) {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = kanjiStrokesAdapter
        kanjiStrokesAdapter.setKanjiStrokesBitmaps(kanjiStrokesBitmaps)
    }

    companion object {
        const val RC_PICK_IMG = 101
    }
}