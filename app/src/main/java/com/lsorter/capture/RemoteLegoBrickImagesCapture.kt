package com.lsorter.capture;

import android.annotation.SuppressLint
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.FLASH_MODE_OFF
import androidx.camera.core.ImageCapture.FLASH_MODE_ON
import androidx.camera.core.ImageProxy
import androidx.camera.core.internal.utils.ImageUtil
import com.google.protobuf.ByteString
import com.lsorter.connection.ConnectionManager
import com.lsorter.detection.detectors.LegoBrickGrpc
import com.lsorter.detection.detectors.LegoBrickProto
import io.grpc.ManagedChannel
import kotlinx.coroutines.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean

class RemoteLegoBrickImagesCapture(private val imageCapture: ImageCapture) :
    LegoBrickDatasetCapture {

    private var captureExecutor: ExecutorService
    private var queueProcessing: Job
    private val connectionManager: ConnectionManager = ConnectionManager()
    private val connectionChannel: ManagedChannel
    private val cameraExecutor: Executor
    private val requestScope: CoroutineScope
    private var requestQueue: ConcurrentLinkedQueue<LegoBrickProto.ImageStore>
    private var onImageCapturedListener: () -> Unit = { }
    private val legoBrickService: LegoBrickGrpc.LegoBrickBlockingStub
    private val canProcessNext: AtomicBoolean
    private var terminated: AtomicBoolean = AtomicBoolean(false)

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
        captureExecutor.submit {
            while (!terminated.get()) {
                if (canProcessNext.get()) {
                    captureImage(label)
                    Thread.sleep(frequencyMs.toLong())
                }
            }
        }
    }

    override fun captureImage(label: String) {
        canProcessNext.set(false)
        synchronized(canProcessNext) {
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
    }

    override fun setOnImageCapturedListener(listener: () -> Unit) {
        this.onImageCapturedListener = listener
    }

    @SuppressLint("CheckResult")
    override fun stop() {
        terminated.set(true)
        queueProcessing.cancel()
        while (!requestQueue.isEmpty()) {
            val request = requestQueue.poll()
            legoBrickService.collectCroppedImages(request)
        }
        connectionChannel.shutdown()
        connectionChannel.awaitTermination(1000, TimeUnit.MILLISECONDS)
    }

    override fun setFlash(enabled: Boolean) {
        synchronized(canProcessNext) {
            this.imageCapture.flashMode = if (enabled) FLASH_MODE_ON else FLASH_MODE_OFF
        }
    }

    private fun startQueueProcessing(): Job {
        return requestScope.launch {
            while (true) {
                if (requestQueue.isEmpty()) continue
                val request = requestQueue.poll()
                legoBrickService.collectCroppedImages(request)
            }
        }
    }

    init {
        this.connectionChannel = connectionManager.getConnectionChannel()
        this.cameraExecutor = Executors.newSingleThreadExecutor()
        this.captureExecutor = Executors.newSingleThreadExecutor()
        this.requestScope = CoroutineScope(Dispatchers.Default)
        this.requestQueue = ConcurrentLinkedQueue()
        this.legoBrickService = LegoBrickGrpc.newBlockingStub(connectionChannel)
        this.canProcessNext = AtomicBoolean(true)
        this.queueProcessing = this.startQueueProcessing()
    }
}
