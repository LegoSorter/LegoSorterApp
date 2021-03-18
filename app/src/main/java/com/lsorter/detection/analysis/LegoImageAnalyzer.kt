package com.lsorter.detection.analysis

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.lsorter.detection.common.DetectedLegoBrick
import com.lsorter.detection.detectors.LegoBrickDetector
import com.lsorter.detection.detectors.LegoBrickDetectorsFactory
import com.lsorter.detection.layer.GraphicOverlay
import com.lsorter.detection.layer.LegoGraphic

class LegoImageAnalyzer(private val graphicOverlay: GraphicOverlay) : ImageAnalysis.Analyzer {
    private val detector: LegoBrickDetector = LegoBrickDetectorsFactory.getLegoBrickDetector()

    private var initialized: Boolean = false
    private var isShutdown: Boolean = false
    private val lock = Any()

    fun shutdown() {
        synchronized(lock) {
            detector.onStop()
            isShutdown = true
        }
    }

    override fun analyze(image: ImageProxy) {
        if (!initialized) {
            if (image.imageInfo.rotationDegrees == 90)
                graphicOverlay.setImageSourceInfo(image.height, image.width)
            else
                graphicOverlay.setImageSourceInfo(image.width, image.height)
            initialized = true
        }

        detector.detectBricks(image).apply {
            image.close()
            drawDetectedBricks(this)
        }
    }

    private fun drawDetectedBricks(bricks: List<DetectedLegoBrick>) {
        synchronized(lock) {
            if (isShutdown) return

            graphicOverlay.clear()

            for (brick in bricks) {
                graphicOverlay.add(LegoGraphic(graphicOverlay, brick))
            }

            graphicOverlay.postInvalidate()
        }
    }
}