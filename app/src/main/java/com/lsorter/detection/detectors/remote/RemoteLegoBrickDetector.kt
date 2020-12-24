package com.lsorter.detection.detectors.remote

import android.annotation.SuppressLint
import android.graphics.Rect
import androidx.camera.core.ImageProxy
import androidx.camera.core.internal.utils.ImageUtil
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.protobuf.ByteString
import com.lsorter.connection.ConnectionManager
import com.lsorter.detection.detectors.LegoBrickDetector
import com.lsorter.detection.detectors.LegoBrickGrpc
import com.lsorter.detection.detectors.LegoBrickProto
import java.util.concurrent.TimeUnit

class RemoteLegoBrickDetector(connectionManager: ConnectionManager) : LegoBrickDetector {

    private val channel = connectionManager.getConnectionChannel()
    private val legoBrickService: LegoBrickGrpc.LegoBrickBlockingStub =
        LegoBrickGrpc.newBlockingStub(channel)

    @SuppressLint("RestrictedApi")
    override fun detectBricks(image: ImageProxy): Task<List<LegoBrickDetector.DetectedLegoBrick>> {
        val request = LegoBrickProto.Image.newBuilder()
            .setImage(
                ByteString.copyFrom(
                    ImageUtil.imageToJpegByteArray(image)
                )
            )
            .setRotation(image.imageInfo.rotationDegrees)
            .build()

        return Tasks.call {
            val boxes = legoBrickService.detectBricks(request)
            val detectedBricks: List<LegoBrickProto.BoundingBoxOrBuilder> =
                boxes.packetOrBuilderList

            detectedBricks.map {
                LegoBrickDetector.DetectedLegoBrick(
                    Rect(it.xmin, it.ymax, it.xmax, it.ymin),
                    LegoBrickDetector.Label(it.score, it.label, 0)
                )
            }
        }
    }

    override fun onStop() {
    }
}