package com.lsorter.capture;

interface LegoBrickDatasetCapture {
    fun captureImages(frequencyMs: Int, label: String)
    fun captureImage(label: String)
    fun setOnImageCapturedListener(listener: () -> Unit)
    fun stop()
    fun setFlash(enabled: Boolean)
}
