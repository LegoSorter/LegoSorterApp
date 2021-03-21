package com.lsorter.analyze.common

import android.graphics.Rect

class RecognizedLegoBrick(val boundingBox: Rect, val label: Label?) {
    class Label(val confidence: Float, val text: String, val index: Int = -1)
}