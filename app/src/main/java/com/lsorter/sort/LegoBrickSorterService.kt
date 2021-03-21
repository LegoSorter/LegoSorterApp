package com.lsorter.sort;

import androidx.camera.core.ImageProxy
import com.lsorter.analyze.common.RecognizedLegoBrick

interface LegoBrickSorterService {
    fun startSorter()
    fun stopSorter()
    fun processImage(image: ImageProxy): List<RecognizedLegoBrick>
    fun updateConfig(configuration: SorterConfiguration)
    fun getConfig(): SorterConfiguration

    class SorterConfiguration
}
