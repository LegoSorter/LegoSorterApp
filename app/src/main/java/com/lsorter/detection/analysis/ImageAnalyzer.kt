package com.lsorter.detection.analysis

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.lsorter.detection.detectors.LegoBrickDetector
import com.lsorter.detection.detectors.LegoBrickDetectorsFactory

class ImageAnalyzer : ImageAnalysis.Analyzer {
    private val detector: LegoBrickDetector = LegoBrickDetectorsFactory.getLegoBrickDetector()

    override fun analyze(image: ImageProxy) {
        val detectBricksTask = detector.detectBricks(image)

        detectBricksTask
            .addOnSuccessListener { /* Store results, inform about results */ }
            .addOnCompleteListener { image.close() }
    }
}