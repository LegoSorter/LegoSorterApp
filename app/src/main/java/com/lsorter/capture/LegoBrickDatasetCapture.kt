package com.lsorter.capture;

import androidx.camera.core.ImageCapture

interface LegoBrickDatasetCapture {
    fun captureImages(imageCapture: ImageCapture, frequencyMs: Int)
    fun stop()
}
