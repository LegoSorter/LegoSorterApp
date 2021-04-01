package com.lsorter.view.settings

import android.content.Context
import android.content.SharedPreferences
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*
import com.lsorter.R
import java.lang.IllegalStateException

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager
            .beginTransaction()
            .replace(android.R.id.content, SettingsFragment(applicationContext))
            .commit()
    }

    class SettingsFragment(private val applicationContext: Context) : PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            (preferenceScreen[0] as PreferenceCategory).forEach(this::updateSummary)
            preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        }

        private fun updateSummary(preference: Preference) {
            when (preference.key) {
                "CAPTURE_MODE_PREFERENCE" -> {
                    preference.apply {
                        val value = preferenceManager.sharedPreferences.getString(key, "Not set")
                            ?: "Not set"

                        this.summary = String.format(
                            applicationContext.getString(R.string.capture_mode_summary),
                            if (value != "Not set") {
                                applicationContext.resources.getStringArray(R.array.capture_mode_items)[Integer.parseInt(
                                    value
                                )]
                            } else
                                value
                        )
                    }
                }
                "EXPOSURE_COMPENSATION_VALUE" -> {
                    preference.apply {
                        val cameraCompensationRange =
                            getBackCameraCharacteristics().get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)
                        this.summary = String.format(
                            applicationContext.getString(R.string.exposure_compensation_summary),
                            cameraCompensationRange?.lower ?: 0,
                            cameraCompensationRange?.upper ?: 0,
                            preferenceManager.sharedPreferences.getString(key, "Not set")
                        )
                    }
                }
                "SENSOR_EXPOSURE_TIME" -> {
                    preference.apply {
                        val exposureTimeRange =
                            getBackCameraCharacteristics().get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
                        this.summary = String.format(
                            applicationContext.getString(R.string.sensor_exposure_time_summary),
                            (exposureTimeRange?.lower ?: 0) / 1e6,
                            (exposureTimeRange?.upper ?: 0) / 1e6,
                            preferenceManager.sharedPreferences.getString(key, "Not set")
                        )
                    }
                }
                "SENSOR_SENSITIVITY" -> {
                    preference.apply {
                        val sensitivityRange =
                            getBackCameraCharacteristics().get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
                        this.summary = String.format(
                            applicationContext.getString(R.string.sensor_sensitivity_summary),
                            sensitivityRange?.lower ?: 0,
                            sensitivityRange?.upper ?: 0,
                            preferenceManager.sharedPreferences.getString(key, "Not set")
                        )
                    }
                }
            }
        }

        private fun getBackCameraCharacteristics(): CameraCharacteristics {
            val cameraManager =
                applicationContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager

            for (id in cameraManager.cameraIdList) {
                val info = cameraManager.getCameraCharacteristics(id)
                if (CameraCharacteristics.LENS_FACING_BACK == info.get(CameraCharacteristics.LENS_FACING))
                    return info
            }

            throw IllegalStateException("No back camera detected!")
        }

        override fun onSharedPreferenceChanged(
            sharedPreferences: SharedPreferences?,
            key: String?
        ) {
            key?.let {
                findPreference<Preference>(it)?.apply { updateSummary(this) }
            }
        }
    }
}