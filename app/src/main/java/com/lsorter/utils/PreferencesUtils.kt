package com.lsorter.utils

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.CaptureRequest
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.core.Camera
import androidx.camera.core.ImageCapture
import androidx.preference.PreferenceManager

class PreferencesUtils {

    companion object {
        @SuppressLint("UnsafeExperimentalUsageError")
        fun applyPreferences(camera: Camera, context: Context) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            preferences.getString("EXPOSURE_COMPENSATION_VALUE", "0")!!.apply {
                camera.cameraControl.setExposureCompensationIndex(this.toInt())
            }
        }

        fun buildImageCapture(context: Context): ImageCapture {
            val builder = ImageCapture.Builder()
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val captureMode = preferences.getString("CAPTURE_MODE_PREFERENCE", "0")!!
            builder.setCaptureMode(if (captureMode == "0") ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY else ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)

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

                } else {
                    val extender = Camera2Interop.Extender(builder)

                    extender.setCaptureRequestOption(
                        CaptureRequest.CONTROL_MODE,
                        CaptureRequest.CONTROL_MODE_AUTO
                    )
                    extender.setCaptureRequestOption(
                        CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON
                    )
                }
            }

            return builder
                .setFlashMode(ImageCapture.FLASH_MODE_OFF)
                .build()
        }
    }
}