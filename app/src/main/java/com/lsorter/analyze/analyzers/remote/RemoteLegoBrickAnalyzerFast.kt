package com.lsorter.analyze.analyzers.remote

import android.annotation.SuppressLint
import android.graphics.Rect
import androidx.camera.core.ImageProxy
import androidx.camera.core.internal.utils.ImageUtil
import com.google.protobuf.ByteString
import com.lsorter.connection.ConnectionManager
import com.lsorter.analyze.common.RecognizedLegoBrick
import com.lsorter.analyze.analyzers.LegoBrickAnalyzerFast
import com.lsorter.analysis.fast.LegoAnalysisFastGrpc
import com.lsorter.common.CommonMessagesProto

class RemoteLegoBrickAnalyzerFast(connectionManager: ConnectionManager) : LegoBrickAnalyzerFast {

    private val channel = connectionManager.getConnectionChannel()
    private val legoAnalysisService: LegoAnalysisFastGrpc.LegoAnalysisFastBlockingStub =
        LegoAnalysisFastGrpc.newBlockingStub(channel)

    override fun detectBricks(image: ImageProxy): List<RecognizedLegoBrick> {
        val request = prepareRequest(image)
        val response = legoAnalysisService.detectBricks(request)

        return mapResponse(response)
    }

    override fun detectAndClassify(image: ImageProxy): List<RecognizedLegoBrick> {
        val request = prepareRequest(image)
        val response = legoAnalysisService.detectAndClassifyBricks(request)

        return mapResponse(response)
    }

    private fun mapResponse(boxes: CommonMessagesProto.ListOfBoundingBoxes): List<RecognizedLegoBrick> {
        val detectedBricks: List<CommonMessagesProto.BoundingBoxOrBuilder> =
            boxes.packetOrBuilderList

        return detectedBricks.map {
            RecognizedLegoBrick(
                Rect(it.xmin, it.ymax, it.xmax, it.ymin),
                RecognizedLegoBrick.Label(
                    it.score,
                    it.label
                )
            )
        }
    }

    @SuppressLint("RestrictedApi")
    private fun prepareRequest(image: ImageProxy): CommonMessagesProto.ImageRequest {
        val request = CommonMessagesProto.ImageRequest.newBuilder()
            .setImage(
                ByteString.copyFrom(
                    ImageUtil.imageToJpegByteArray(image)
                )
            )
            .setRotation(image.imageInfo.rotationDegrees)
            .build()
        return request
    }

    override fun onStop() {
    }
}