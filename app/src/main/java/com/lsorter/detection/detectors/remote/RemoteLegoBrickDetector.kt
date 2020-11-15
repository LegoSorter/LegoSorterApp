package com.lsorter.detection.detectors.remote

import android.annotation.SuppressLint
import androidx.camera.core.ImageProxy
import androidx.camera.core.internal.utils.ImageUtil
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.protobuf.ByteString
import com.lsorter.detection.detectors.LegoBrickDetector
import com.lsorter.detection.detectors.LegoBrickGrpc
import com.lsorter.detection.detectors.LegoBrickProto
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder

class RemoteLegoBrickDetector : LegoBrickDetector {

    // TODO Read an address from properties
    private val channel: ManagedChannel = ManagedChannelBuilder.forAddress("10.0.2.2", 50051)
        .usePlaintext()
        .build()

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
            legoBrickService.recognizeLegoBrickInImage(request)
            // TODO - return list of detected lego bricks
            emptyList<LegoBrickDetector.DetectedLegoBrick>()
        }
    }
}