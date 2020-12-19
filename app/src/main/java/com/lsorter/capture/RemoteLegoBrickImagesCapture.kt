package com.lsorter.capture;

import android.annotation.SuppressLint
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.internal.utils.ImageUtil
import com.google.protobuf.ByteString
import com.lsorter.connection.ConnectionManager
import com.lsorter.detection.detectors.LegoBrickGrpc
import com.lsorter.detection.detectors.LegoBrickProto
import kotlinx.coroutines.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class RemoteLegoBrickImagesCapture(val connectionManager: ConnectionManager) : LegoBrickDatasetCapture {

    private val cameraExecutor: Executor = Executors.newSingleThreadExecutor()
    private var scope = MainScope()

    private val legoBrickService: LegoBrickGrpc.LegoBrickBlockingStub =
            LegoBrickGrpc.newBlockingStub(connectionManager.getConnectionChannel())

    @SuppressLint("RestrictedApi", "CheckResult")
    fun sendLegoImageWithLabel(image: ImageProxy, label: String) {
        val request = LegoBrickProto.ImageStore.newBuilder()
                .setImage(
                        ByteString.copyFrom(
                                ImageUtil.imageToJpegByteArray(image)
                        )
                )
                .setRotation(image.imageInfo.rotationDegrees)
                .setLabel(label)
                .build()

        legoBrickService.collectImages(request)
    }

    override fun captureImages(imageCapture: ImageCapture, frequencyMs: Int) {
        scope.launch {
            val counter = AtomicInteger(0)
            while (true) {
                if (counter.get() == 0) {
                    imageCapture.takePicture(cameraExecutor,
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    sendLegoImageWithLabel(image, "lego")
                                    image.close()
                                    counter.decrementAndGet()
                                }
                            })
                    counter.incrementAndGet()
                }

                delay(frequencyMs.toLong())
            }
        }
    }

    override fun stop() {
        connectionManager.getConnectionChannel().shutdown()
        connectionManager.getConnectionChannel().awaitTermination(1000, TimeUnit.MILLISECONDS)
        scope.cancel()
        scope = MainScope()
    }
}
