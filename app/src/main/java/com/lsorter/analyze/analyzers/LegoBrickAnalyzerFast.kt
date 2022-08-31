package com.lsorter.analyze.analyzers

import androidx.camera.core.ImageProxy
import com.lsorter.analyze.common.RecognizedLegoBrick

interface LegoBrickAnalyzerFast {

    fun detectBricks(image: ImageProxy): List<RecognizedLegoBrick>

    fun detectAndClassify(image: ImageProxy,session: String): List<RecognizedLegoBrick>

    fun onStop()

}