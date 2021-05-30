package com.lsorter.sort

import android.annotation.SuppressLint
import android.graphics.Rect
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.internal.utils.ImageUtil
import com.google.protobuf.ByteString
import com.lsorter.analyze.common.RecognizedLegoBrick
import com.lsorter.common.CommonMessagesProto
import com.lsorter.connection.ConnectionManager
import com.lsorter.sorter.LegoSorterGrpc
import com.lsorter.sorter.LegoSorterProto
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class DefaultLegoBrickSorterService : LegoBrickSorterService {
    private val captureExecutor: ExecutorService = Executors.newFixedThreadPool(2)
    private var terminated: AtomicBoolean = AtomicBoolean(false)
    private var canProcessNext: AtomicBoolean = AtomicBoolean(true)

    private val connectionManager: ConnectionManager = ConnectionManager()
    private val legoSorterService: LegoSorterGrpc.LegoSorterBlockingStub

    init {
        this.legoSorterService =
            LegoSorterGrpc.newBlockingStub(connectionManager.getConnectionChannel())
    }

    @SuppressLint("CheckResult")
    override fun startMachine() {
        this.legoSorterService.startMachine(CommonMessagesProto.Empty.getDefaultInstance())
    }

    @SuppressLint("CheckResult")
    override fun stopMachine() {
        this.legoSorterService.stopMachine(CommonMessagesProto.Empty.getDefaultInstance())
    }

    @SuppressLint("RestrictedApi")
    override fun processImage(image: ImageProxy): List<RecognizedLegoBrick> {
        val imageRequest = CommonMessagesProto.ImageRequest.newBuilder()
            .setImage(
                ByteString.copyFrom(
                    ImageUtil.imageToJpegByteArray(image)
                )
            ).setRotation(image.imageInfo.rotationDegrees)
            .build()
        image.close()

        return mapResponse(legoSorterService.processNextImage(imageRequest))
    }

    @SuppressLint("CheckResult")
    override fun updateConfig(configuration: LegoBrickSorterService.SorterConfiguration) {
        val configRequest = LegoSorterProto.SorterConfiguration.newBuilder()
            .setSpeed(configuration.speed)
            .build()

        legoSorterService.updateConfiguration(configRequest)
    }

    override fun getConfig(): LegoBrickSorterService.SorterConfiguration {
        TODO("Not yet implemented")
    }

    override fun scheduleImageCapturingAndStartMachine(
        imageCapture: ImageCapture,
        runTime: Int,
        callback: (ImageProxy) -> Unit
    ) {
        terminated.set(false)
        canProcessNext.set(true)
        captureExecutor.submit {
            while (!terminated.get()) {
                synchronized(canProcessNext) {
                    if (canProcessNext.get()) {
                        canProcessNext.set(false)
                        stopMachine()
                        captureImage(imageCapture) { image ->
                            callback(image)
                            if (!terminated.get()) {
                                this.legoSorterService.startMachine(CommonMessagesProto.Empty.getDefaultInstance())
                                Thread.sleep(runTime.toLong())
                                canProcessNext.set(true)
                            }
                        }
                    }
                    Thread.sleep(10)
                }
            }
        }
    }

    override fun stopImageCapturing() {
        terminated.set(true)
        stopMachine()
    }

    private fun captureImage(
        imageCapture: ImageCapture,
        callback: (ImageProxy) -> Unit
    ) {
        imageCapture.takePicture(
            captureExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) = callback(image)
            }
        )
    }

    private fun mapResponse(boxes: LegoSorterProto.ListOfBoundingBoxesWithIndexes): List<RecognizedLegoBrick> {
        val detectedBricks = boxes.packetOrBuilderList

        return detectedBricks.map {
            RecognizedLegoBrick(
                Rect(it.bb.xmin, it.bb.ymax, it.bb.xmax, it.bb.ymin),
                RecognizedLegoBrick.Label(
                    it.bb.score,
                    it.bb.label,
                    it.index
                )
            )
        }
    }
}