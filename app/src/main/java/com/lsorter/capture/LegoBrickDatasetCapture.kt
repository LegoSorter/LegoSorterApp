package com.lsorter.capture;

import androidx.camera.core.ImageCapture

interface LegoBrickDatasetCapture {
    fun captureImages(imageCapture: ImageCapture, frequencyMs: Int, label: String)
    fun setOnImageCapturedListener(listener: () -> Unit)
    fun stop()
}
