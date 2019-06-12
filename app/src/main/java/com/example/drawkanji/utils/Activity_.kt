package com.example.drawkanji.utils

import android.app.Activity
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.example.drawkanji.R
import com.google.android.material.snackbar.Snackbar

enum class SnackbarType(@ColorRes val colorId: Int) {
    ERROR(R.color.error), INFO(R.color.info)
}

fun Activity.snackbar(
    view: View,
    message: String,
    length: Int,
    snackbarType: SnackbarType = SnackbarType.INFO
) {
    val snack = Snackbar.make(view, message, length)
    snack.view.setBackgroundColor(ContextCompat.getColor(this, snackbarType.colorId))
    snack.show()
}

