package com.lsorter.detection.detectors.remote

import android.annotation.SuppressLint
import androidx.camera.core.ImageProxy
import androidx.camera.core.internal.utils.ImageUtil
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.protobuf.ByteString
import com.lsorter.connection.ConnectionManager
import com.lsorter.detection.detectors.LegoBrickDetector
import com.lsorter.detection.detectors.LegoBrickGrpc
import com.lsorter.detection.detectors.LegoBrickProto

class RemoteLegoBrickDetector(connectionManager: ConnectionManager) : LegoBrickDetector {

    private val legoBrickService: LegoBrickGrpc.LegoBrickBlockingStub =
        LegoBrickGrpc.newBlockingStub(connectionManager.getConnectionChannel())

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
            legoBrickService.recognizeLegoBrickInImage(request)
            // TODO - return list of detected lego bricks
            emptyList<LegoBrickDetector.DetectedLegoBrick>()
        }
    }
}