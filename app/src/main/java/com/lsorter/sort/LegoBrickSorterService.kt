package com.lsorter.sort

import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import com.lsorter.analyze.common.RecognizedLegoBrick

interface LegoBrickSorterService {
    fun startMachine()
    fun stopMachine()
    fun processImage(image: ImageProxy): List<RecognizedLegoBrick>
    fun queueImage(image: ImageProxy)
    fun updateConfig(configuration: SorterConfiguration)
    fun getConfig(): SorterConfiguration
    fun scheduleImageCapturingAndStartMachine(
        imageCapture: ImageCapture,
        runTime: Int,
        callback: (image: ImageProxy) -> Unit
    )
    fun scheduleImageCapturingContinuous(
            imageCapture: ImageCapture,
            fps: Int,
            callback: (image: ImageProxy) -> Unit
    )

    fun stopImageCapturing()

    data class SorterConfiguration(val speed: Int = 50)
}