package com.lsorter.analyze.analyzers.remote

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import androidx.camera.core.ImageProxy
import androidx.camera.core.internal.utils.ImageUtil
import androidx.preference.PreferenceManager
import com.google.protobuf.ByteString
import com.lsorter.App.Companion.applicationContext
import com.lsorter.App.Companion.sharedPreferences
import com.lsorter.R
import com.lsorter.analysis.fast.LegoAnalysisFastGrpc
import com.lsorter.analysis.fast.LegoAnalysisFastProto
import com.lsorter.analyze.analyzers.LegoBrickAnalyzerFast
import com.lsorter.analyze.common.RecognizedLegoBrick
import com.lsorter.common.CommonMessagesProto
import com.lsorter.connection.ConnectionManager
import io.grpc.stub.StreamObserver
import kotlin.system.measureTimeMillis


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
    var cnt =0;
    override fun detectAndClassify(image: ImageProxy,session: String): List<RecognizedLegoBrick> {
        val elapsed = measureTimeMillis {
            cnt++;
            val request = prepareRequest2(image,session)
            val response = legoAnalysisService.detectAndClassifyBricks(request, ProductCallback())
//            Thread.sleep(100)
        }
        println("${System.nanoTime()};$cnt;Detection took;$elapsed");
//        try {
//            val pref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext())
            var savedSessionLong:Long?=null
            val elapsed2 = measureTimeMillis {
                var prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext());
                var savedSession = prefs.getString("ANALYSIS_MINIMUM_DELAY", "350") ?: "350"
                savedSessionLong = savedSession.toLongOrNull()
            }
//            println("${System.nanoTime()};$cnt;Detection took;$elapsed2");
            if (savedSessionLong != null && elapsed<savedSessionLong!!){
                Thread.sleep(savedSessionLong!!-elapsed-elapsed2)
            }

//        }
//        catch (e:Exception){
//
//        }

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

    @SuppressLint("RestrictedApi")
    private fun prepareRequest2(image: ImageProxy,session:String): LegoAnalysisFastProto.FastImageRequest {
        val request = LegoAnalysisFastProto.FastImageRequest.newBuilder()
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
            .setSession(session)
            .build()
        return request
    }

    override fun onStop() {
    }
}