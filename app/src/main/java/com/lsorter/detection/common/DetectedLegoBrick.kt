package com.lsorter.detection.common

import android.graphics.Rect

class DetectedLegoBrick(val boundingBox: Rect, val label: Label?) {
    class Label(val confidence: Float, val text: String, val index: Int)
}