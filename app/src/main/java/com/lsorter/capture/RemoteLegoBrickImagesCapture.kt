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

class RemoteLegoBrickImagesCapture(private val imageCapture: ImageCapture) :
    LegoBrickDatasetCapture {

    private val connectionManager = ConnectionManager()
    private val connectionChannel = connectionManager.getConnectionChannel()
    private val cameraExecutor: Executor = Executors.newSingleThreadExecutor()
    private var scope = MainScope()
    private val requestScope = CoroutineScope(Dispatchers.Default)
    private var requestQueue = ConcurrentLinkedQueue<LegoBrickProto.ImageStore>()
    private var onImageCapturedListener: () -> Unit = { }
    private val legoBrickService: LegoBrickGrpc.LegoBrickBlockingStub =
        LegoBrickGrpc.newBlockingStub(connectionChannel)

    private val canProcessNext: AtomicBoolean = AtomicBoolean(true)

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
    }

    override fun captureImages(frequencyMs: Int, label: String) {
        scope.launch {
            while (true) {
                if (canProcessNext.get()) {
                    val sound = MediaActionSound()
                    sound.play(MediaActionSound.SHUTTER_CLICK)
                    captureImage(label)
                    delay(frequencyMs.toLong())
                }
            }
        }

        requestScope.launch {
            while (true) {
                if (requestQueue.isEmpty()) continue
                val request = requestQueue.poll()
                legoBrickService.collectCroppedImages(request)
            }
        }
    }

    override fun captureImage(label: String) {
        canProcessNext.set(false)
        imageCapture.takePicture(cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    sendLegoImageWithLabel(image, label)
                    image.close()
                    canProcessNext.set(true)
                    onImageCapturedListener()
                }
            })
    }

    override fun setOnImageCapturedListener(listener: () -> Unit) {
        this.onImageCapturedListener = listener
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
