package com.lsorter.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.hardware.camera2.CaptureRequest
import android.util.Size
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.core.*
import androidx.preference.PreferenceManager

class PreferencesUtils {

    companion object {
        @SuppressLint("UnsafeExperimentalUsageError")
        fun applyPreferences(camera: Camera, context: Context?) {
            context?.apply {
                val preferences = PreferenceManager.getDefaultSharedPreferences(context)
                preferences.getString("EXPOSURE_COMPENSATION_VALUE", "0")!!.apply {
                    camera.cameraControl.setExposureCompensationIndex(this.toInt())
                }
            }
        }

        fun extendImageCapture(
            builder: ImageCapture.Builder,
            context: Context?
        ): ImageCapture.Builder {
            context?.apply {
                val preferences = PreferenceManager.getDefaultSharedPreferences(context)
                val captureMode = preferences.getString("CAPTURE_MODE_PREFERENCE", "0")!!
                val resolution = preferences.getString("CAPTURE_RESOLUTION_VALUE", "0")!!.let {
                    when (it) {
                        "0" -> RESOLUTION_SD
                        "1" -> RESOLUTION_HD
                        "2" -> RESOLUTION_FHD
                        "3" -> RESOLUTION_2K
                        "4" -> RESOLUTION_UHD
                        else -> RESOLUTION_UHD
                    }
                }
                builder.setCaptureMode(if (captureMode == "0") ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY else ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                builder.setTargetResolution(resolution)

                extendByPreferences(builder, preferences)
            }
            return builder.setFlashMode(ImageCapture.FLASH_MODE_OFF)
        }

        fun extendPreviewView(
            builder: Preview.Builder,
            context: Context?
        ): Preview.Builder {
            context?.apply {
                val preferences = PreferenceManager.getDefaultSharedPreferences(context)
                extendByPreferences(builder, preferences)
            }
            return builder
        }

        fun extendImageAnalysis(
            builder: ImageAnalysis.Builder,
            context: Context?
        ): ImageAnalysis.Builder {
            context?.apply {
                val preferences = PreferenceManager.getDefaultSharedPreferences(context)
                val resolution = preferences.getString("ANALYSIS_RESOLUTION_VALUE", "0")!!.let {
                    when (it) {
                        "0" -> RESOLUTION_QVGA
                        "1" -> RESOLUTION_SD
                        "2" -> RESOLUTION_HD
                        else -> RESOLUTION_FHD
                    }
                }
                builder.setTargetResolution(resolution)
                extendByPreferences(builder, preferences)
            }
            return builder
        }

        private fun <T> extendByPreferences(
            builder: ExtendableBuilder<T>,
            preferences: SharedPreferences
        ) {
            preferences.getBoolean("MANUAL_SETTINGS", false).apply {
                if (this) {
                    val extender = Camera2Interop.Extender(builder)

                    extender.setCaptureRequestOption(
                        CaptureRequest.CONTROL_MODE,
                        CaptureRequest.CONTROL_MODE_AUTO
                    )
                    extender.setCaptureRequestOption(
                        CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_OFF
                    )

                    preferences.getString("SENSOR_EXPOSURE_TIME", "")!!.apply {
                        if (this.isNotEmpty()) {
                            val exposureTimeNs = (this.toFloat() * 1e6).toLong()
                            extender.setCaptureRequestOption(
                                CaptureRequest.SENSOR_EXPOSURE_TIME,
                                exposureTimeNs
                            )
                        }
                    }
                    preferences.getString("SENSOR_SENSITIVITY", "")!!.apply {
                        if (this.isNotEmpty()) {
                            val sensorSensitivity = this.toInt()
                            extender.setCaptureRequestOption(
                                CaptureRequest.SENSOR_SENSITIVITY,
                                sensorSensitivity
                            )
                        }
                    }
                }
            }
        }

        private val RESOLUTION_QVGA = Size(240, 320)
        private val RESOLUTION_SD = Size(480, 640)
        private val RESOLUTION_HD = Size(720, 1280)
        private val RESOLUTION_FHD = Size(1080, 1920)
        private val RESOLUTION_2K = Size(1152, 2048)
        private val RESOLUTION_UHD = Size(2160, 3840)
    }
}