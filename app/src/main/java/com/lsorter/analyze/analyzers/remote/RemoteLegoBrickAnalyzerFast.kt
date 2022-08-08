package com.lsorter.analyze.analyzers.remote

import android.annotation.SuppressLint
import android.graphics.Rect
import androidx.camera.core.ImageProxy
import androidx.camera.core.internal.utils.ImageUtil
import com.google.protobuf.ByteString
import com.lsorter.analysis.fast.LegoAnalysisFastGrpc
import com.lsorter.analyze.analyzers.LegoBrickAnalyzerFast
import com.lsorter.analyze.common.RecognizedLegoBrick
import com.lsorter.common.CommonMessagesProto
import com.lsorter.connection.ConnectionManager
import io.grpc.stub.StreamObserver


class RemoteLegoBrickAnalyzerFast(connectionManager: ConnectionManager) : LegoBrickAnalyzerFast {
    internal class ProductCallback : StreamObserver<CommonMessagesProto.ListOfBoundingBoxes?> {
        override fun onNext(value: CommonMessagesProto.ListOfBoundingBoxes?) {
//            if (value != null) {
//                var response = mapResponse(value)
//            }
//            log.info("Received product, {}", value)
        }

        override fun onError(cause: Throwable) {
//            log.error("Error occurred, cause {}", cause.message)
        }

        override fun onCompleted() {
//            log.info("Stream completed")
        }

//        private fun mapResponse(boxes: CommonMessagesProto.ListOfBoundingBoxes): List<RecognizedLegoBrick> {
//            val detectedBricks: List<CommonMessagesProto.BoundingBoxOrBuilder> =
//                boxes.packetOrBuilderList
//
//            return detectedBricks.map {
//                RecognizedLegoBrick(
//                    Rect(it.xmin, it.ymax, it.xmax, it.ymin),
//                    RecognizedLegoBrick.Label(
//                        it.score,
//                        it.label
//                    )
//                )
//            }
//        }
    }

    private val channel = connectionManager.getConnectionChannel()
    private val legoAnalysisService: LegoAnalysisFastGrpc.LegoAnalysisFastStub =
        LegoAnalysisFastGrpc.newStub(channel)

    override fun detectBricks(image: ImageProxy): List<RecognizedLegoBrick> {
        val request = prepareRequest(image)
        val response = legoAnalysisService.detectBricks(request, ProductCallback())

        return emptyList()
    }

    override fun detectAndClassify(image: ImageProxy): List<RecognizedLegoBrick> {
        val request = prepareRequest(image)
        val response = legoAnalysisService.detectAndClassifyBricks(request, ProductCallback())

        return emptyList()
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
                    ImageUtil.yuvImageToJpegByteArray(
                        image,
                        if (ImageUtil.shouldCropImage(image)) image.cropRect else null,
                        100
                    )
                )
            )
            .setRotation(image.imageInfo.rotationDegrees)
            .build()
        return request
    }

    override fun onStop() {
    }
}