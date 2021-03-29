package com.lsorter.sort

import android.annotation.SuppressLint
import android.graphics.Rect
import androidx.camera.core.ImageProxy
import androidx.camera.core.internal.utils.ImageUtil
import com.google.protobuf.ByteString
import com.lsorter.analyze.common.RecognizedLegoBrick
import com.lsorter.common.CommonMessagesProto
import com.lsorter.connection.ConnectionManager
import com.lsorter.sorter.LegoSorterGrpc
import com.lsorter.sorter.LegoSorterProto

class DefaultLegoBrickSorterService : LegoBrickSorterService {
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

        val response = legoSorterService.processNextImage(imageRequest)
        return mapResponse(response)
    }

    override fun updateConfig(configuration: LegoBrickSorterService.SorterConfiguration) {
        TODO("Not yet implemented")
    }

    override fun getConfig(): LegoBrickSorterService.SorterConfiguration {
        TODO("Not yet implemented")
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