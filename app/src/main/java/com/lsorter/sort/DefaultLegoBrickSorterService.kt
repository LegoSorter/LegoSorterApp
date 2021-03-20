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

class DefaultLegoBrickSorterService : LegoBrickSorterService {
    private val connectionManager: ConnectionManager = ConnectionManager()
    private val legoSorterService: LegoSorterGrpc.LegoSorterBlockingStub

    init {
        this.legoSorterService =
            LegoSorterGrpc.newBlockingStub(connectionManager.getConnectionChannel())
    }

    override fun startSorter() {
        TODO("Not yet implemented")
    }

    override fun stopSorter() {
        TODO("Not yet implemented")
    }

    @SuppressLint("RestrictedApi")
    override fun sendImage(image: ImageProxy): List<RecognizedLegoBrick> {
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

    private fun mapResponse(boxes: CommonMessagesProto.ListOfBoundingBoxes): List<RecognizedLegoBrick> {
        val detectedBricks: List<CommonMessagesProto.BoundingBoxOrBuilder> =
            boxes.packetOrBuilderList

        return detectedBricks.map {
            RecognizedLegoBrick(
                Rect(it.xmin, it.ymax, it.xmax, it.ymin),
                RecognizedLegoBrick.Label(
                    it.score,
                    it.label,
                    0
                )
            )
        }
    }
}