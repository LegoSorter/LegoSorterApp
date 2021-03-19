package com.lsorter.detection.detectors.remote

import android.annotation.SuppressLint
import android.graphics.Rect
import androidx.camera.core.ImageProxy
import androidx.camera.core.internal.utils.ImageUtil
import com.google.protobuf.ByteString
import com.lsorter.connection.ConnectionManager
import com.lsorter.detection.common.DetectedLegoBrick
import com.lsorter.detection.detectors.LegoBrickDetector
import com.lsorter.analysis.LegoAnalysisGrpc
import com.lsorter.common.CommonMessagesProto

class RemoteLegoBrickDetector(connectionManager: ConnectionManager) : LegoBrickDetector {

    private val channel = connectionManager.getConnectionChannel()
    private val legoBrickService: LegoAnalysisGrpc.LegoAnalysisBlockingStub =
        LegoAnalysisGrpc.newBlockingStub(channel)

    @SuppressLint("RestrictedApi")
    override fun detectBricks(image: ImageProxy): List<DetectedLegoBrick> {
        val request = CommonMessagesProto.ImageRequest.newBuilder()
            .setImage(
                ByteString.copyFrom(
                    ImageUtil.imageToJpegByteArray(image)
                )
            )
            .setRotation(image.imageInfo.rotationDegrees)
            .build()

        val boxes = legoBrickService.detectBricks(request)
        val detectedBricks: List<CommonMessagesProto.BoundingBoxOrBuilder> =
            boxes.packetOrBuilderList

        return detectedBricks.map {
            DetectedLegoBrick(
                Rect(it.xmin, it.ymax, it.xmax, it.ymin),
                DetectedLegoBrick.Label(
                    it.score,
                    it.label,
                    0
                )
            )

        }
    }

    override fun onStop() {
    }
}