package com.lsorter.sort;

import androidx.camera.core.ImageProxy
import com.lsorter.detection.common.DetectedLegoBrick

interface LegoBrickSorterService {
    fun startSorter()
    fun stopSorter()
    fun sendImage(image: ImageProxy): List<DetectedLegoBrick>
    fun updateConfig(configuration: SorterConfiguration)
    fun getConfig(): SorterConfiguration

    class SorterConfiguration
}
