package com.lsorter.detection.detectors

import android.annotation.SuppressLint
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions

class OnPremiseLegoBrickDetector : LegoBrickDetector {

    // TODO: replace with a custom detector
    private val detector: ObjectDetector = ObjectDetection.getClient(getObjectDetectorOptions())

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun detectBricks(image: ImageProxy): Task<List<LegoBrickDetector.DetectedLegoBrick>> {
        val inputImage = InputImage.fromMediaImage(image.image!!, image.imageInfo.rotationDegrees)

        return detector.process(inputImage).continueWith { result ->
            result.result!!.map {
                val first = it.labels.first()
                val label = LegoBrickDetector.Label(first.confidence, first.text, first.index)
                LegoBrickDetector.DetectedLegoBrick(it.boundingBox, label)
            }
        }
    }

    private fun getObjectDetectorOptions(): ObjectDetectorOptions {
        return ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableClassification()
            .enableMultipleObjects()
            .build()
    }
}