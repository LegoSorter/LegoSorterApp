package com.lsorter.detection.detectors

import android.graphics.Rect
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task

interface LegoBrickDetector {

    fun detectBricks(image: ImageProxy): Task<List<DetectedLegoBrick>>

    class DetectedLegoBrick(
        boundingBox: Rect,
        label: Label
    ) {}

    class Label(
        confidence: Float,
        text: String,
        index: Int
    ) {}
}