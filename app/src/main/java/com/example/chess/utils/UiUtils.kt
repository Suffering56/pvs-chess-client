package com.example.chess.utils

import android.view.View
import android.widget.EditText

fun View.changeSize(width: Int, height: Int = width) {
    this.layoutParams.width = width
    this.layoutParams.height = height
    this.requestLayout()
}

fun EditText.getTextAsInt() = this.text.toString().toInt()

fun EditText.getTextAsLong() = this.text.toString().toLong()