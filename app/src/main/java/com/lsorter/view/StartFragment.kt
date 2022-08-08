package com.lsorter.view

//import android.R
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
//import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import com.lsorter.App
import com.lsorter.R
import com.lsorter.databinding.FragmentStartBinding
import com.lsorter.view.analyzefast.AnalyzeFastFragment
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers


//import kotlinx.serialization.encodeToString
//import kotlinx.serialization.json.Json
//import kotlinx.serialization.Serializable

//@Serializable
data class Configs(
    val capture_mode_preference: String,
    val capture_resolution_value: String,
    val analysis_resolution_value: String,
    val exposure_compensation_value: String,
    val manual_settings: Boolean,
    val sensor_exposure_time: String,
    val sensor_sensitivity: String,
    val sorter_conveyor_speed_value: Int,
    val sorter_mode_preference: String,
    val run_conveyor_time_value: String
)

data class ConfigsConstraints(
    val cameraCompensationRangeMin: Int,
    val cameraCompensationRangeMax: Int,
    val exposureTimeRangeMin: Double,
    val exposureTimeRangeMax: Double,
    val sensitivityRangeMin: Int,
    val sensitivityRangeMax: Int
)

class StartFragment : Fragment() {
    var hubConnection: HubConnection? = null;
    var hubConnectionConnecting: Boolean = false;
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentStartBinding.inflate(inflater, container, false)
        setupRemoteControlStart(binding)
        setupNavigation(binding)
        val savedAddr = activity?.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )?.getString(
            getString(R.string.saved_server_address_key),
            "ip:port"
        ) ?: "ip:port"
        binding.serverAddressBox.setText(savedAddr)

        binding.serverAddressBox.setOnFocusChangeListener { a, hasFocus ->
            if (!hasFocus) {
                val v: View? = activity?.findViewById(android.R.id.content)
                val imm: InputMethodManager? =
                    activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm?.hideSoftInputFromWindow(v?.getWindowToken(), 0)

                val sharedPref = activity?.getSharedPreferences(
                    getString(R.string.preference_file_key), Context.MODE_PRIVATE
                )
                if (sharedPref != null) {
                    with(sharedPref.edit()) {
                        putString(
                            getString(R.string.saved_server_address_key),
                            binding.serverAddressBox.text.toString()
                        )
                        apply()
                    }
                }
            }
        }

        val savedWebAddr = activity?.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )?.getString(
            getString(R.string.saved_web_server_address_key),
            "ip:port"
        ) ?: "ip:port"
        binding.webServerAddressBox.setText(savedWebAddr)

        binding.webServerAddressBox.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val v: View? = activity?.findViewById(android.R.id.content)
                val imm: InputMethodManager? =
                    activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm?.hideSoftInputFromWindow(v?.getWindowToken(), 0)

                val sharedPref = activity?.getSharedPreferences(
                    getString(R.string.preference_file_key), Context.MODE_PRIVATE
                )
                if (sharedPref != null) {
                    with(sharedPref.edit()) {
                        putString(
                            getString(R.string.saved_web_server_address_key),
                            binding.webServerAddressBox.text.toString()
                        )
                        apply()
                    }
                }
            }
        }

        return binding.root
    }

    private fun setupRemoteControlStart(binding: FragmentStartBinding) {
        binding.webRemote.setOnClickListener {
            setupRemoteControl(binding)
        }
        if (this.hubConnection == null) {
            binding.webRemote.text = getString(R.string.web_remote_start)
        } else {
            binding.webRemote.text = getString(R.string.web_remote_stop)
        }
    }

    private fun setupRemoteControl(binding: FragmentStartBinding) {
        if (hubConnectionConnecting) {
            return;
        }
        if (this.hubConnection != null) {
            this.hubConnection!!.stop()
            this.hubConnection!!.close()
            this.hubConnection = null
            binding.webRemote.text = getString(R.string.web_remote_start)
            return
        }
        this.hubConnectionConnecting = true;
        binding.webRemote.text = getString(R.string.web_remote_connecting)
        val addrPref = App.sharedPreferences().getString(
            App.applicationContext().getString(R.string.saved_web_server_address_key),
            "10.0.2.2:50051"
        ) ?: "10.0.2.2:50051"
        val hubConnection: HubConnection =
            HubConnectionBuilder.create("${addrPref}/hubs/control").build()
//            HubConnectionBuilder.create("http://192.168.11.189:5002/hubs/control").build()

//        val hubConnection: HubConnection = this.hubConnection!!

        hubConnection.on(
            "messageReceived",
            { message: String -> println("New Message: $message") },
            String::class.java
        )
        hubConnection.on(
            "navigation",
            { message: String ->
                run {
                    when (message) {
                        "analyzeFast" -> {
                            val navHostFragment = findNavController()
                            navHostFragment.navigate(StartFragmentDirections.actionStartFragmentToAnalyzeFastFragment())
                        }
                        "analyze" -> {
                            val navHostFragment = findNavController()
                            navHostFragment.navigate(StartFragmentDirections.actionStartFragmentToAnalyzeFragment())
                        }
                        "sort" -> {
                            val navHostFragment = findNavController()
                            navHostFragment.navigate(StartFragmentDirections.actionStartFragmentToSortFragment())
                        }
                        "back" -> {
                            val navHostFragment = findNavController()
                            navHostFragment.navigateUp()
//                            navHostFragment.navigate(StartFragmentDirections.actionStartFragmentToCaptureDialogFragment())
                        }
                        else -> {
                            println("New Message: $message")
                        }
                    }
                }
            },
            String::class.java
        )

        hubConnection.on(
            "action",
            { message: String ->
                run {
                    when (message) {
                        "analyzeFast" -> {
                            val currentFragment = findNavController().currentDestination?.id
                            if (currentFragment == R.id.analyzeFastFragment) {
                                try {
                                    val fm: FragmentManager? = activity?.supportFragmentManager
                                    fm?.fragments?.forEach {
                                        it.childFragmentManager.fragments.forEach { fragment ->
                                            if (fragment is AnalyzeFastFragment) {
                                                if (!fragment.analysisStarted) {
                                                    activity?.runOnUiThread(Runnable {
                                                        // Stuff that updates the UI
                                                        fragment.binding.startStopSortingButton.text =
                                                            getString(R.string.stop_text)
                                                        fragment.analyzeImages()
                                                        fragment.analysisStarted = true
                                                    })


                                                } else {
                                                    activity?.runOnUiThread(Runnable {
                                                        fragment.binding.startStopSortingButton.text =
                                                            getString(R.string.start_text)
                                                        fragment.stopImageAnalysis()
                                                        fragment.analysisStarted = false

                                                    })
                                                }
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    val te = 5

                                }
//                                val fragment: Fragment =
//                                    getSupportFragmentManager().findFragmentById(id)
                                //do something
                            }
//                            val navHostFragment = findNavController()
//                            navHostFragment.navigate(StartFragmentDirections.actionStartFragmentToAnalyzeFastFragment())
                        }
//                        "analyze" -> {
//                            val navHostFragment = findNavController()
//                            navHostFragment.navigate(StartFragmentDirections.actionStartFragmentToAnalyzeFragment())
//                        }
//                        "sort" -> {
//                            val navHostFragment = findNavController()
//                            navHostFragment.navigate(StartFragmentDirections.actionStartFragmentToSortFragment())
//                        }
//                        "captureDialog" -> {
//                            val navHostFragment = findNavController()
//                            navHostFragment.navigate(StartFragmentDirections.actionStartFragmentToCaptureDialogFragment())
//                        }
                        else -> {
                            println("New Message: $message")
                        }
                    }
                }
            },
            String::class.java
        )
//        hubConnection.< String, Foo<String>>on<kotlin.String?, Foo<kotlin.String?>?>("func", { param1, param2 ->
//            System.out.println(param1)
//            System.out.println(param2)
//        }, kotlin.String::class.java, fooType)

        hubConnection.on(
            "getConfig",
            { option: String ->
                run {
                    val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

                    when (option) {
                        "CAPTURE_MODE_PREFERENCE" -> {
                            hubConnection.send("returnConfig", option, pref.getString(option, "0"))
                        }
                        "CAPTURE_RESOLUTION_VALUE" -> {
                            hubConnection.send("returnConfig", option, pref.getString(option, "0"))
                        }
                        "ANALYSIS_RESOLUTION_VALUE" -> {
                            hubConnection.send("returnConfig", option, pref.getString(option, "0"))
                        }
                        "EXPOSURE_COMPENSATION_VALUE" -> {
                            hubConnection.send("returnConfig", option, pref.getString(option, "0"))
                        }
                        "MANUAL_SETTINGS" -> {
                            hubConnection.send(
                                "returnConfig",
                                option,
                                pref.getBoolean(option, false).toString()
                            )
                        }
                        "SENSOR_EXPOSURE_TIME" -> {
                            hubConnection.send("returnConfig", option, pref.getString(option, ""))
                        }
                        "SENSOR_SENSITIVITY" -> {
                            hubConnection.send("returnConfig", option, pref.getString(option, ""))
                        }
                        "SORTER_CONVEYOR_SPEED_VALUE" -> {
                            hubConnection.send(
                                "returnConfig",
                                option,
                                pref.getInt(option, 50).toString()
                            )
                        }
                        "SORTER_MODE_PREFERENCE" -> {
                            hubConnection.send("returnConfig", option, pref.getString(option, "0"))
                        }
                        "RUN_CONVEYOR_TIME_VALUE" -> {
                            hubConnection.send(
                                "returnConfig",
                                option,
                                pref.getString(option, "500")
                            )
                        }
                        else -> {
                            println("New Message: $option")
                        }
                    }
                }
            },
            String::class.java
        )

//        hubConnection.on("Send") { System.out.println("New Message: $message") }
        hubConnection.on(
            "getConfigs"
        ) {
            val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
            var configs = Configs(
                capture_mode_preference = pref.getString("CAPTURE_MODE_PREFERENCE", "0")!!,
                capture_resolution_value = pref.getString("CAPTURE_RESOLUTION_VALUE", "0")!!,
                analysis_resolution_value = pref.getString("ANALYSIS_RESOLUTION_VALUE", "0")!!,
                exposure_compensation_value = pref.getString("EXPOSURE_COMPENSATION_VALUE", "0")!!,
                manual_settings = pref.getBoolean("MANUAL_SETTINGS", false),
                sensor_exposure_time = pref.getString("SENSOR_EXPOSURE_TIME", "")!!,
                sensor_sensitivity = pref.getString("SENSOR_SENSITIVITY", "")!!,
                sorter_conveyor_speed_value = pref.getInt("SORTER_CONVEYOR_SPEED_VALUE", 50),
                sorter_mode_preference = pref.getString("SORTER_MODE_PREFERENCE", "0")!!,
                run_conveyor_time_value = pref.getString("RUN_CONVEYOR_TIME_VALUE", "500")!!
            )
//            try {
//                var json = Json.encodeToString(configs)
//
//            }
//            catch (e:Exception){
//                var t ="t"
//            }
            hubConnection.send("sendConfigs", configs)

        }
        hubConnection.on("getConfigsConstraints") {
            var cameraCompensationRangeMin: Int = 0
            var cameraCompensationRangeMax: Int = 0
            var exposureTimeRangeMin: Double = 0.0
            var exposureTimeRangeMax: Double = 0.0
            var sensitivityRangeMin: Int = 0
            var sensitivityRangeMax: Int = 0
            val cameraManager = context?.getSystemService(Context.CAMERA_SERVICE) as CameraManager

            for (id in cameraManager.cameraIdList) {
                val info = cameraManager.getCameraCharacteristics(id)
                if (CameraCharacteristics.LENS_FACING_BACK == info.get(CameraCharacteristics.LENS_FACING)) {
                    cameraCompensationRangeMin =
                        info.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)?.lower ?: 0
                    cameraCompensationRangeMax =
                        info.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)?.upper ?: 0
                    exposureTimeRangeMin =
                        (info.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)?.lower
                            ?: 0) / 1e6
                    exposureTimeRangeMax =
                        (info.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)?.upper
                            ?: 0) / 1e6
                    sensitivityRangeMin =
                        info.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)?.lower ?: 0
                    sensitivityRangeMax =
                        info.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)?.upper ?: 0
                    break
                }
            }

            var configsConstraints = ConfigsConstraints(
                cameraCompensationRangeMin = cameraCompensationRangeMin,
                cameraCompensationRangeMax = cameraCompensationRangeMax,
                exposureTimeRangeMin = exposureTimeRangeMin,
                exposureTimeRangeMax = exposureTimeRangeMax,
                sensitivityRangeMin = sensitivityRangeMin,
                sensitivityRangeMax = sensitivityRangeMax
            )

            hubConnection.send("sendConfigsConstraints", configsConstraints)
        }

        hubConnection.on(
            "setConfigs",
            { config: Configs ->
                run {
                    val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
                    val editor = pref.edit()

                    editor.putString("CAPTURE_MODE_PREFERENCE" , config.capture_mode_preference)
                    editor.putString( "CAPTURE_RESOLUTION_VALUE", config.capture_resolution_value)
                    editor.putString("ANALYSIS_RESOLUTION_VALUE", config.analysis_resolution_value)
                    editor.putString( "EXPOSURE_COMPENSATION_VALUE", config.exposure_compensation_value)
                    editor.putBoolean("MANUAL_SETTINGS", config.manual_settings)
                    editor.putString("SENSOR_EXPOSURE_TIME", config.sensor_exposure_time)
                    editor.putString("SENSOR_SENSITIVITY", config.sensor_sensitivity)
                    editor.putInt("SORTER_CONVEYOR_SPEED_VALUE", config.sorter_conveyor_speed_value)
                    editor.putString("SORTER_MODE_PREFERENCE", config.sorter_mode_preference)
                    editor.putString("RUN_CONVEYOR_TIME_VALUE", config.run_conveyor_time_value)
                    editor.apply()


                    val currentFragment = findNavController().currentDestination?.id
                    if (currentFragment == R.id.analyzeFastFragment) {
                        try {
                            val fm: FragmentManager? = activity?.supportFragmentManager
                            fm?.fragments?.forEach {
                                it.childFragmentManager.fragments.forEach { fragment ->
                                    if (fragment is AnalyzeFastFragment) {
                                        if (!fragment.analysisStarted) {

                                            activity?.runOnUiThread(Runnable {
                                                // Stuff that updates the UI
                                                fragment.stopCamera()
                                                fragment.prepareCamera()
                                                val te = 5
                                            })

                                        }
                                        else {

                                            activity?.runOnUiThread(Runnable {
                                                fragment.stopCamera()
                                                fragment.analyzeImages()
                                                val te = 5
                                            })
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            val te = 5

                        }
//                                val fragment: Fragment =
//                                    getSupportFragmentManager().findFragmentById(id)
                        //do something
                    }
                }
            },
            Configs::class.java
        )

        hubConnection.on(
            "setConfig",
            { option: String, value: String ->
                run {
                    val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
                    val editor = pref.edit()

                    when (option) {
                        "CAPTURE_MODE_PREFERENCE" -> {
                            editor.putString(option, value)
                            editor.apply()
                        }
                        "CAPTURE_RESOLUTION_VALUE" -> {
                            editor.putString(option, value)
                            editor.apply()
                        }
                        "ANALYSIS_RESOLUTION_VALUE" -> {
                            editor.putString(option, value)
                            editor.apply()
                        }
                        "EXPOSURE_COMPENSATION_VALUE" -> {
                            editor.putString(option, value)
                            editor.apply()
                        }
                        "MANUAL_SETTINGS" -> {
                            editor.putBoolean(option, value.toBoolean())
                            editor.apply()
                        }
                        "SENSOR_EXPOSURE_TIME" -> {
                            editor.putString(option, value)
                            editor.apply()
                        }
                        "SENSOR_SENSITIVITY" -> {
                            editor.putString(option, value)
                            editor.apply()
                        }
                        "SORTER_CONVEYOR_SPEED_VALUE" -> {
                            editor.putInt(option, value.toInt())
                            editor.apply()
                        }
                        "SORTER_MODE_PREFERENCE" -> {
                            editor.putString(option, value)
                            editor.apply()
                        }
                        "RUN_CONVEYOR_TIME_VALUE" -> {
                            editor.putString(option, value)
                            editor.apply()
                        }
                        else -> {
                            println("New Message: $option")
                        }
                    }
                }
            },
            String::class.java, String::class.java
        )

        hubConnection.start().doOnError {
            hubConnection.stop()
            hubConnection.close()
            this.hubConnectionConnecting = false;
//            this.hubConnection = null
            activity?.runOnUiThread(Runnable {
                Toast.makeText(
                    requireContext(),
                    "Error connecting to UI server:\n$addrPref",
                    Toast.LENGTH_LONG
                ).show()
                binding.webRemote.text = getString(R.string.web_remote_start)
            })
        }.doOnComplete {
            this.hubConnection = hubConnection
            this.hubConnectionConnecting = false;
            activity?.runOnUiThread(Runnable {
                binding.webRemote.text = getString(R.string.web_remote_stop)
            })
        }.subscribe({}, {})


//        while (hubConnection.connectionState==HubConnectionState.CONNECTING){
//            Thread.sleep(1);
//        }
//        if (hubConnection.connectionState==HubConnectionState.CONNECTED)
//            hubConnection.send("register","test")
//        if (hubConnection.connectionState==HubConnectionState.DISCONNECTED){
//            Toast.makeText(requireContext(),"Error connecting to UI server:\n$addrPref",Toast.LENGTH_LONG).show()
//            hubConnection.stop()
//            hubConnection.close()
//            this.hubConnection = null
//            binding.webRemote.text=getString(R.string.web_remote_start)
//        }
//        try {
//
//        }
//        catch (e: Exception){
//            Toast.makeText(requireContext(),"Error connecting to UI server:\n$addrPref",Toast.LENGTH_LONG).show()
//            hubConnection.stop()
//            hubConnection.close()
//            this.hubConnection = null
//            binding.webRemote.text=getString(R.string.web_remote_start)
//        }

    }


    private fun setupNavigation(binding: FragmentStartBinding) {
        binding.createDatasetButton.setOnClickListener(
            Navigation.createNavigateOnClickListener(
                StartFragmentDirections.actionStartFragmentToCaptureDialogFragment()
            )
        )

        binding.analyzeButton.setOnClickListener(
            Navigation.createNavigateOnClickListener(
                StartFragmentDirections.actionStartFragmentToAnalyzeFragment()
            )
        )

        binding.sortButton.setOnClickListener(
            Navigation.createNavigateOnClickListener(
                StartFragmentDirections.actionStartFragmentToSortFragment()
            )
        )

        binding.analyzeFastButton.setOnClickListener(
            Navigation.createNavigateOnClickListener(
                StartFragmentDirections.actionStartFragmentToAnalyzeFastFragment()
            )
        )
    }
}