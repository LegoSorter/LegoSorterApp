package com.lsorter.analyze

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.lsorter.analyze.common.RecognizedLegoBrick
import com.lsorter.analyze.analyzers.LegoBrickAnalyzerFast
import com.lsorter.analyze.analyzers.LegoBrickAnalyzersFastFactory
import com.lsorter.analyze.layer.GraphicOverlay
import com.lsorter.analyze.layer.LegoGraphic

class LegoImageAnalyzerFast(private val graphicOverlay: GraphicOverlay,private val session:String) : ImageAnalysis.Analyzer {
    private val analyzer: LegoBrickAnalyzerFast = LegoBrickAnalyzersFastFactory.getLegoBrickAnalyzer()

    private var initialized: Boolean = false
    private var isShutdown: Boolean = false
    private var analysisMode: AnalysisMode = AnalysisMode.DETECT_AND_CLASSIFY
    private val lock = Any()

    fun shutdown() {
        synchronized(lock) {
            analyzer.onStop()
            isShutdown = true
        }
    }

    fun setAnalysisMode(mode: AnalysisMode) {
        analysisMode = mode
    }

    override fun analyze(image: ImageProxy) {
        if (!initialized) {
            graphicOverlay.setImageSourceInfo(
                image.width,
                image.height,
                image.imageInfo.rotationDegrees
            )
            initialized = true
        }

        if (analysisMode == AnalysisMode.DETECT_ONLY) {
            analyzer.detectBricks(image)
        } else {
            analyzer.detectAndClassify(image,session)
        }.apply {
            image.close()
            drawRecognizedBricks(this)
        }
    }

    private fun drawRecognizedBricks(bricks: List<RecognizedLegoBrick>) {
        synchronized(lock) {
            if (isShutdown) return

            graphicOverlay.clear()

            for (brick in bricks) {
                graphicOverlay.add(LegoGraphic(graphicOverlay, brick))
            }

            graphicOverlay.postInvalidate()
        }
    }

    enum class AnalysisMode {
        DETECT_ONLY,
        DETECT_AND_CLASSIFY
    }
}