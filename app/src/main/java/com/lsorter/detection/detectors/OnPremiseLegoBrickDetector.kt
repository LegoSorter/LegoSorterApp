package com.lsorter.detection.detectors

import android.annotation.SuppressLint
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.lsorter.detection.common.DetectedLegoBrick

class OnPremiseLegoBrickDetector : LegoBrickDetector {

    // TODO: replace with a custom detector
    private val detector: ObjectDetector = ObjectDetection.getClient(getObjectDetectorOptions())

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun detectBricks(image: ImageProxy): List<DetectedLegoBrick> {
        val inputImage = InputImage.fromMediaImage(image.image!!, image.imageInfo.rotationDegrees)
        val processing = detector.process(inputImage)
        processing.addOnCompleteListener { image.close() }

        return processing.result!!.map {
            var label: DetectedLegoBrick.Label? = null

            if (it.labels.isNotEmpty()) {
                val first = it.labels.first()
                label = DetectedLegoBrick.Label(
                    first.confidence,
                    first.text,
                    first.index
                )
            }

            DetectedLegoBrick(
                it.boundingBox,
                label
            )
        }
    }

    private fun getObjectDetectorOptions(): ObjectDetectorOptions {
        return ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableClassification()
            .enableMultipleObjects()
            .build()
    }

    override fun onStop() {

    }
}