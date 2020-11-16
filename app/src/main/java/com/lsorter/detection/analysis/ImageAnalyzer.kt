package com.lsorter.detection.analysis

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.lsorter.detection.detectors.LegoBrickDetector
import com.lsorter.detection.detectors.LegoBrickDetectorsFactory
import com.lsorter.detection.layer.GraphicOverlay
import com.lsorter.detection.layer.LegoGraphic
import java.lang.Exception
import java.lang.RuntimeException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ImageAnalyzer(private val graphicOverlay: GraphicOverlay) : ImageAnalysis.Analyzer {
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val detector: LegoBrickDetector = LegoBrickDetectorsFactory.getLegoBrickDetector()

    private var initialized: Boolean = false
    private var isShutdown: Boolean = false
    private val lock = Any()

    fun shutdown() {
        synchronized(lock) {
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

        detector.detectBricks(image)
            .addOnSuccessListener(executor, OnSuccessListener { drawDetectedBricks(it) })
            .addOnFailureListener(executor, OnFailureListener { onFailure(it) })
            .addOnCompleteListener(executor, OnCompleteListener { image.close() })
    }

    private fun drawDetectedBricks(bricks: List<LegoBrickDetector.DetectedLegoBrick>) {
        synchronized(lock) {
            if (isShutdown) return

            graphicOverlay.clear()

            for (brick in bricks) {
                graphicOverlay.add(LegoGraphic(graphicOverlay, brick))
            }

            graphicOverlay.postInvalidate()
        }
    }

    private fun onFailure(e: Exception) {
        Log.e(ImageAnalyzer::class.java.name, "Exception during detecting bricks", e)
        throw RuntimeException(e)
    }
}