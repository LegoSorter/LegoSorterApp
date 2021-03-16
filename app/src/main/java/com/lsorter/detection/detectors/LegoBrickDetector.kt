package com.lsorter.detection.detectors

import android.graphics.Rect
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task

interface LegoBrickDetector {

    fun detectBricks(image: ImageProxy): List<DetectedLegoBrick>

    fun onStop()

    class DetectedLegoBrick(val boundingBox: Rect, val label: Label?) {

    }

    class Label(val confidence: Float, val text: String, val index: Int) {

    }
}