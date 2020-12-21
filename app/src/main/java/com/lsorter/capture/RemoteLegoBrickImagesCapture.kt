package com.lsorter.capture;

import android.annotation.SuppressLint
import android.media.MediaActionSound
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
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.ConcurrentLinkedQueue

class RemoteLegoBrickImagesCapture : LegoBrickDatasetCapture {

    private val connectionManager = ConnectionManager()
    private val connectionChannel = connectionManager.getConnectionChannel()
    private val cameraExecutor: Executor = Executors.newSingleThreadExecutor()
    private var scope = MainScope()
    private val requestScope = CoroutineScope(Dispatchers.Default)
    private var requestQueue = ConcurrentLinkedQueue<LegoBrickProto.ImageStore>()

    private val legoBrickService: LegoBrickGrpc.LegoBrickBlockingStub =
            LegoBrickGrpc.newBlockingStub(connectionChannel)

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
        requestQueue.add(request)
//        legoBrickService.collectCroppedImages(request)
    }

    override fun captureImages(imageCapture: ImageCapture, frequencyMs: Int, label: String) {
        scope.launch {
            val canProcessNext = AtomicBoolean(true)
            while (true) {
                if (canProcessNext.get()) {
                    canProcessNext.set(false)
                    val sound = MediaActionSound()
                    sound.play(MediaActionSound.SHUTTER_CLICK)
                    imageCapture.takePicture(cameraExecutor,
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    sendLegoImageWithLabel(image, label)
                                    image.close()
                                    canProcessNext.set(true)
                                }
                            })
                }
                delay(frequencyMs.toLong())
            }
        }
        requestScope.launch {
            while (true) {
                if (requestQueue.isEmpty()) continue
                var request = requestQueue.poll()
                legoBrickService.collectCroppedImages(request)
            }
        }
    }

    override fun stop() {
        requestScope.cancel()
        scope.cancel()
        scope = MainScope()
        while (!requestQueue.isEmpty()) {
            var request = requestQueue.poll()
            legoBrickService.collectCroppedImages(request)
        }
        connectionChannel.shutdown()
        connectionChannel.awaitTermination(1000, TimeUnit.MILLISECONDS)
    }
}
