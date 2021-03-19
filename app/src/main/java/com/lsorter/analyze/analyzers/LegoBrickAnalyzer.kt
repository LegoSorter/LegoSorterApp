package com.lsorter.analyze.analyzers

import androidx.camera.core.ImageProxy
import com.lsorter.analyze.common.RecognizedLegoBrick

interface LegoBrickAnalyzer {

    fun detectBricks(image: ImageProxy): List<RecognizedLegoBrick>

    fun detectAndClassify(image: ImageProxy): List<RecognizedLegoBrick>

    fun onStop()

}