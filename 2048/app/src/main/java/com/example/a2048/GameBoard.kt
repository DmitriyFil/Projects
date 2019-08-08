package com.example.a2048

import android.widget.Button

fun setStartText(btns: Array<Button>) {
    for (b in btns) {
        b.setText("")
        b.setTextSize(30F)
    }
}

fun randomFreeTile(btns: Array<Button>): Button {
    val bl = ArrayList<Button>()
    for (b in btns) {
        if (b.text.toString().isEmpty()) {
            bl.add(b)
        }
    }
    return bl.random()
}

fun resetGameBoard(btns: Array<Button>) {
    for (b in btns) {
        if (b.text.toString().isNotEmpty()) {
            b.setText("")
            b.setBackgroundColor(0xFFCDC1B5.toInt())
        }
    }
}





