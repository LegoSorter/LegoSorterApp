package com.lsorter.analyze.analyzers

import android.annotation.SuppressLint
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.lsorter.analyze.common.RecognizedLegoBrick

class OnPremiseLegoBrickAnalyzer : LegoBrickAnalyzer {

    // TODO: replace with a custom detector
    private val detector: ObjectDetector = ObjectDetection.getClient(getObjectDetectorOptions())

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun detectBricks(image: ImageProxy): List<RecognizedLegoBrick> {
        val inputImage = InputImage.fromMediaImage(image.image!!, image.imageInfo.rotationDegrees)
        val processing = detector.process(inputImage)
        processing.addOnCompleteListener { image.close() }

        return processing.result!!.map {
            var label: RecognizedLegoBrick.Label? = null

            if (it.labels.isNotEmpty()) {
                val first = it.labels.first()
                label = RecognizedLegoBrick.Label(
                    first.confidence,
                    first.text,
                    first.index
                )
            }

            RecognizedLegoBrick(
                it.boundingBox,
                label
            )
        }
    }

    override fun detectAndClassify(image: ImageProxy): List<RecognizedLegoBrick> {
        TODO("Not yet implemented")
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