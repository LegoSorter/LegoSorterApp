package com.lsorter.detection.detectors

import androidx.camera.core.ImageProxy
import com.lsorter.detection.common.DetectedLegoBrick

interface LegoBrickDetector {

    fun detectBricks(image: ImageProxy): List<DetectedLegoBrick>

    fun onStop()

}