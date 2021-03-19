package com.lsorter.capture;

import android.annotation.SuppressLint
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.FLASH_MODE_OFF
import androidx.camera.core.ImageCapture.FLASH_MODE_ON
import androidx.camera.core.ImageProxy
import androidx.camera.core.internal.utils.ImageUtil
import com.google.protobuf.ByteString
import com.lsorter.connection.ConnectionManager
import io.grpc.ManagedChannel
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean

class RemoteLegoBrickImagesCapture(private val imageCapture: ImageCapture) :
    LegoBrickDatasetCapture {

    private var queueProcessingExecutor: ExecutorService
    private var captureExecutor: ExecutorService
    private val cameraExecutor: ExecutorService
    private val connectionManager: ConnectionManager = ConnectionManager()
    private val connectionChannel: ManagedChannel
    private val imageCaptureLock = Any()
    private var requestQueue: ConcurrentLinkedQueue<LegoCaptureProto.ImageStore>
    private var onImageCapturedListener: () -> Unit = { }
    private val legoBrickService: LegoCaptureGrpc.LegoCaptureFutureStub
    private val canProcessNext: AtomicBoolean
    private var terminated: AtomicBoolean = AtomicBoolean(false)

    @SuppressLint("RestrictedApi", "CheckResult")
    fun sendLegoImageWithLabel(image: ImageProxy, label: String) {
        val request = LegoCaptureProto.ImageStore.newBuilder()
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
        synchronized(imageCaptureLock) {
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

    override fun setFlash(enabled: Boolean) {
        synchronized(imageCaptureLock) {
            this.imageCapture.flashMode = if (enabled) FLASH_MODE_ON else FLASH_MODE_OFF
        }
    }

    private fun startQueueProcessing() {
        this.queueProcessingExecutor.submit {
            while (!terminated.get() || !requestQueue.isEmpty()) {
                if (requestQueue.isEmpty()) continue
                val request = requestQueue.poll()
                print("Queue - processing next request\n")
                legoBrickService.collectCroppedImages(request)
                print("Queue - done\n")
            }
        }
    }

    @SuppressLint("CheckResult")
    override fun stop() {
        terminated.set(true)
        finishQueue()
        captureExecutor.shutdown()
        cameraExecutor.shutdown()
        queueProcessingExecutor.shutdown()
        queueProcessingExecutor.awaitTermination(1000, TimeUnit.MILLISECONDS)
    }

    private fun finishQueue() {
        // TODO: Extract whole queue to an independent service
        while (!requestQueue.isEmpty()) {
            Thread.sleep(5)
        }
    }

    init {
        this.connectionChannel = connectionManager.getConnectionChannel()
        this.cameraExecutor = Executors.newSingleThreadExecutor()
        this.captureExecutor = Executors.newSingleThreadExecutor()
        this.queueProcessingExecutor = Executors.newFixedThreadPool(1)
        this.requestQueue = ConcurrentLinkedQueue()
        this.legoBrickService = LegoCaptureGrpc.newFutureStub(connectionChannel)
            .withWaitForReady()
        this.canProcessNext = AtomicBoolean(true)

        startQueueProcessing()
    }
}
