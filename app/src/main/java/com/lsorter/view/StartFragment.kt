package com.lsorter.view

//import android.R
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.view.KeyEvent
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
import com.lsorter.view.analyze.AnalyzeFragment
import com.lsorter.view.analyzefast.AnalyzeFastFragment
import com.lsorter.view.capture.CaptureFragment
import com.lsorter.view.sort.SortFragment
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
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
    val run_conveyor_time_value: String,
    val analysis_minimum_delay: String,
    val render_belt_speed: String,
    val render_belt_opacity: String,
    val render_belt_camera_view: Boolean
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
            "server.sorter.ml:50051"
        ) ?: "server.sorter.ml:50051" //  ip:port
        binding.serverAddressBox.setText(savedAddr)

        binding.serverAddressBox.setOnKeyListener { view, i, keyEvent ->
            when {
                //Check if it is the Enter-Key,      Check if the Enter Key was pressed down
                ((keyEvent.action == KeyEvent.ACTION_DOWN) && (i == KeyEvent.KEYCODE_ENTER)) -> {

                    val imm: InputMethodManager? =
                        activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                    imm?.hideSoftInputFromWindow(view?.getWindowToken(), 0)

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
                    binding.serverAddressBox.clearFocus()
                    return@setOnKeyListener true
                }
                else -> false
            }

        }

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
            "http://sorter.ml"
        ) ?: "http://sorter.ml" //  http://ip:port
        binding.webServerAddressBox.setText(savedWebAddr)

        binding.webServerAddressBox.setOnKeyListener { view, i, keyEvent ->
            when {
                //Check if it is the Enter-Key,      Check if the Enter Key was pressed down
                ((keyEvent.action == KeyEvent.ACTION_DOWN) && (i == KeyEvent.KEYCODE_ENTER)) -> {

                    val imm: InputMethodManager? =
                        activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                    imm?.hideSoftInputFromWindow(view?.getWindowToken(), 0)

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
                    binding.webServerAddressBox.clearFocus()
                    return@setOnKeyListener true
                }
                else -> false
            }

        }

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
            if (this.hubConnection!!.connectionState == HubConnectionState.CONNECTED)
                this.hubConnection!!.send("sendEndPong")
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
            "http://sorter.ml"
        ) ?: "http://sorter.ml" //  http://ip:port
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
                            activity?.runOnUiThread(Runnable {
                                navHostFragment.navigate(StartFragmentDirections.actionStartFragmentToAnalyzeFastFragment())
//                                sendState(hubConnection)
                            })
                            hubConnection.send("returnState", "analyzeFastFragment")
                        }
                        "analyze" -> {
                            val navHostFragment = findNavController()
                            activity?.runOnUiThread(Runnable {
                                navHostFragment.navigate(StartFragmentDirections.actionStartFragmentToAnalyzeFragment())
//                                sendState(hubConnection)
                            })
                            hubConnection.send("returnState", "analyzeFragment")
                        }
                        "sort" -> {
                            val navHostFragment = findNavController()
                            activity?.runOnUiThread(Runnable {
                                navHostFragment.navigate(StartFragmentDirections.actionStartFragmentToSortFragment())
//                                sendState(hubConnection)
                            })
                            hubConnection.send("returnState", "sortFragmentOff")
                        }
                        "back" -> {
                            val navHostFragment = findNavController()
                            activity?.runOnUiThread(Runnable {
                                navHostFragment.navigateUp()
//                                sendState(hubConnection)
                            })
                            hubConnection.send("returnState", "startFragment")
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
                                                        sendState(hubConnection)
                                                    })


                                                } else {
                                                    activity?.runOnUiThread(Runnable {
                                                        fragment.binding.startStopSortingButton.text =
                                                            getString(R.string.start_text)
                                                        fragment.stopImageAnalysis()
                                                        fragment.analysisStarted = false
                                                        sendState(hubConnection)
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
                        "analyze" -> {
                            val currentFragment = findNavController().currentDestination?.id
                            if (currentFragment == R.id.analyzeFragment) {
                                try {
                                    val fm: FragmentManager? = activity?.supportFragmentManager
                                    fm?.fragments?.forEach {
                                        it.childFragmentManager.fragments.forEach { fragment ->
                                            if (fragment is AnalyzeFragment) {
                                                if (!fragment.analysisStarted) {
                                                    activity?.runOnUiThread(Runnable {
                                                        // Stuff that updates the UI
                                                        fragment.binding.startStopSortingButton.text =
                                                            getString(R.string.stop_text)
                                                        fragment.analyzeImages()
                                                        fragment.analysisStarted = true
                                                        sendState(hubConnection)
                                                    })


                                                } else {
                                                    activity?.runOnUiThread(Runnable {
                                                        fragment.binding.startStopSortingButton.text =
                                                            getString(R.string.start_text)
                                                        fragment.stopImageAnalysis()
                                                        fragment.analysisStarted = false
                                                        sendState(hubConnection)
                                                    })
                                                }
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    val te = 5

                                }
                            }
                        }
                        "sortMachine" -> {
                            val currentFragment = findNavController().currentDestination?.id
                            if (currentFragment == R.id.sortFragment) {
                                try {
                                    val fm: FragmentManager? = activity?.supportFragmentManager
                                    fm?.fragments?.forEach {
                                        it.childFragmentManager.fragments.forEach { fragment ->
                                            if (fragment is SortFragment) {
                                                if (!fragment.isMachineStarted.get()) {
                                                    activity?.runOnUiThread(Runnable {
                                                        // Stuff that updates the UI
                                                        fragment.isMachineStarted.set(true)
                                                        fragment.binding.startStopMachineButton.text =
                                                            getString(com.lsorter.R.string.stop_machine_text)
                                                        fragment.startMachine()
                                                        sendState(hubConnection)
                                                    })


                                                } else {
                                                    activity?.runOnUiThread(Runnable {
                                                        fragment.isMachineStarted.set(false)
                                                        fragment.binding.startStopMachineButton.text =
                                                            getString(com.lsorter.R.string.start_machine_text)
                                                        fragment.stopMachine()
                                                        sendState(hubConnection)
                                                    })
                                                }
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    val te = 5

                                }
                            }
                        }
                        "sort" -> {
                            val currentFragment = findNavController().currentDestination?.id
                            if (currentFragment == R.id.sortFragment) {
                                try {
                                    val fm: FragmentManager? = activity?.supportFragmentManager
                                    fm?.fragments?.forEach {
                                        it.childFragmentManager.fragments.forEach { fragment ->
                                            if (fragment is SortFragment) {
                                                if (!fragment.isSortingStarted.get()) {
                                                    activity?.runOnUiThread(Runnable {
                                                        // Stuff that updates the UI
                                                        fragment.setVisibilityOfFocusSeeker(View.GONE)
                                                        fragment.binding.startStopSortingButton.text =
                                                            getString(com.lsorter.R.string.stop_sorting_text)
                                                        fragment.startSorting()
                                                        fragment.isSortingStarted.set(true)
                                                        sendState(hubConnection)
                                                    })


                                                } else {
                                                    activity?.runOnUiThread(Runnable {
                                                        fragment.isSortingStarted.set(false)
                                                        fragment.setVisibilityOfFocusSeeker(View.VISIBLE)
                                                        fragment.binding.startStopSortingButton.text =
                                                            getString(com.lsorter.R.string.start_sorting_text)
                                                        fragment.stopSorting()
                                                        sendState(hubConnection)
                                                    })
                                                }
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    val te = 5

                                }
                            }
                        }
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
                        "ANALYSIS_MINIMUM_DELAY" -> {
                            hubConnection.send("returnConfig", option, pref.getString(option, "750"))
                        }
                        "RENDER_BELT_SPEED" -> {
                            hubConnection.send("returnConfig", option, pref.getString(option, "1"))
                        }
                        "RENDER_OPACITY" -> {
                            hubConnection.send("returnConfig", option, pref.getString(option, "75"))
                        }
                        "CAMERA_VIEW" -> {
                            hubConnection.send(
                                "returnConfig",
                                option,
                                pref.getBoolean(option, false).toString()
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

        hubConnection.on("sendPing") {
            hubConnection.send("sendPong")
        }

        hubConnection.on("getState") {
            sendState(hubConnection)
        }

        hubConnection.on("getConnectionConfigs") {
            val savedAddr = activity?.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE
            )?.getString(
                getString(R.string.saved_server_address_key),
                "server.sorter.ml:50051"
            ) ?: "server.sorter.ml:50051" //  ip:port

            val savedWebAddr = activity?.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE
            )?.getString(
                getString(R.string.saved_web_server_address_key),
                "http://sorter.ml"
            ) ?: "http://sorter.ml" //  http://ip:port

            hubConnection.send("sendConnectionConfigs", savedAddr, savedWebAddr)
        }

        hubConnection.on(
            "setConnectionConfigs", { savedAddr: String, savedWebAddr: String ->
                run {
                    val sharedPref = activity?.getSharedPreferences(
                        getString(R.string.preference_file_key), Context.MODE_PRIVATE
                    )
                    if (sharedPref != null) {
                        with(sharedPref.edit()) {
                            putString(
                                getString(R.string.saved_server_address_key),
                                savedAddr
                            )
                            putString(
                                getString(R.string.saved_web_server_address_key),
                                savedWebAddr
                            )
                            apply()
                        }
                    }
                    activity?.runOnUiThread(Runnable {
                        // Stuff that updates the UI
                        binding.serverAddressBox.setText(savedAddr)
                        binding.webServerAddressBox.setText(savedWebAddr)
                        setupRemoteControl(binding) //stop
                        setupRemoteControl(binding) //restart
                    })
                }
            },
            String::class.java, String::class.java
        )

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
                run_conveyor_time_value = pref.getString("RUN_CONVEYOR_TIME_VALUE", "500")!!,
                analysis_minimum_delay = pref.getString("ANALYSIS_MINIMUM_DELAY", "750")!!,
                render_belt_speed = pref.getString("RENDER_BELT_SPEED", "1.0")!!,
                render_belt_opacity = pref.getString("RENDER_OPACITY", "75")!!,
                render_belt_camera_view = pref.getBoolean("CAMERA_VIEW", false)
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
            "getSession"
        ) {
            run {
                val savedSession = activity?.getSharedPreferences(
                    getString(R.string.preference_file_key), Context.MODE_PRIVATE
                )?.getString(
                    "user_saved_session_value",
                    "Session_1"
                ) ?: "Session_1"
//                val savedSession = activity?.getSharedPreferences(
//                    getString(R.string.preference_file_key), Context.MODE_PRIVATE
//                )?.getString(
//                    getString(R.string.saved_session_value_key),
//                    "Session_1"
//                ) ?: "Session_1"
                val saveImgSwitchVal = activity?.getSharedPreferences(
                    getString(R.string.preference_file_key), Context.MODE_PRIVATE
                )?.getBoolean(getString(R.string.saved_image_switch_key), false) ?: false

                hubConnection.send("sendSession", saveImgSwitchVal, savedSession)
            }
        }

        hubConnection.on(
            "setSession", { saveImg: Boolean, session: String ->
                run {
                    val pref = PreferenceManager.getDefaultSharedPreferences(requireContext());
                    val sharedPref = activity?.getSharedPreferences(
                        getString(R.string.preference_file_key), Context.MODE_PRIVATE
                    )
                    if (sharedPref != null) {
                        with(sharedPref.edit()) {
                            putString(
                                getString(R.string.saved_session_value_key),
                                session
                            )
                            apply()
                        }
                    }
                    val editor = pref.edit();
//                    editor.putString(getString(R.string.saved_session_value_key), session)
//                    editor.putString("user_saved_session_value", "test");
                    editor.putBoolean(getString(R.string.saved_image_switch_key), saveImg);
                    editor.apply();
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
                                                fragment.binding.saveImgSwitch.isChecked = saveImg;
                                                fragment.binding.sessionName.setText(session);
//                                                fragment.stopCamera();
//                                                fragment.prepareCamera();
//                                                val te = 5
                                            })

                                        } else {

                                            activity?.runOnUiThread(Runnable {
                                                fragment.binding.saveImgSwitch.isChecked = saveImg;
                                                fragment.binding.sessionName.setText(session);
                                                fragment.stopCamera();
                                                fragment.analyzeImages();
                                                val te = 5
                                            })
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            val te = 5

                        }
                    }
                }
            },
            Boolean::class.java, String::class.java
        )

        hubConnection.on(
            "setConfigs",
            { config: Configs ->
                run {
                    val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
                    val editor = pref.edit()

                    editor.putString("CAPTURE_MODE_PREFERENCE", config.capture_mode_preference)
                    editor.putString("CAPTURE_RESOLUTION_VALUE", config.capture_resolution_value)
                    editor.putString("ANALYSIS_RESOLUTION_VALUE", config.analysis_resolution_value)
                    editor.putString(
                        "EXPOSURE_COMPENSATION_VALUE",
                        config.exposure_compensation_value
                    )
                    editor.putBoolean("MANUAL_SETTINGS", config.manual_settings)
                    editor.putString("SENSOR_EXPOSURE_TIME", config.sensor_exposure_time)
                    editor.putString("SENSOR_SENSITIVITY", config.sensor_sensitivity)
                    editor.putInt("SORTER_CONVEYOR_SPEED_VALUE", config.sorter_conveyor_speed_value)
                    editor.putString("SORTER_MODE_PREFERENCE", config.sorter_mode_preference)
                    editor.putString("RUN_CONVEYOR_TIME_VALUE", config.run_conveyor_time_value)
                    editor.putString("ANALYSIS_MINIMUM_DELAY", config.analysis_minimum_delay)
                    editor.putString("RENDER_BELT_SPEED", config.render_belt_speed)
                    editor.putString("RENDER_OPACITY", config.render_belt_opacity)
                    editor.putBoolean("CAMERA_VIEW", config.render_belt_camera_view)
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

                                        } else {

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
                        "ANALYSIS_MINIMUM_DELAY" -> {
                            editor.putString(option, value)
                            editor.apply()
                        }
                        "RENDER_OPACITY" -> {
                            editor.putString(option, value)
                            editor.apply()
                        }
                        "RENDER_BELT_SPEED" -> {
                            editor.putString(option, value)
                            editor.apply()
                        }
                        "CAMERA_VIEW" -> {
                            editor.putBoolean(option, value.toBoolean())
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
            hubConnection.send("sendPong")
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
    private fun sendState(hubConnection:HubConnection){
        val currentFragment = findNavController().currentDestination?.id
        if (currentFragment == R.id.analyzeFastFragment) {
            try {
                val fm: FragmentManager? = activity?.supportFragmentManager
                fm?.fragments?.forEach {
                    it.childFragmentManager.fragments.forEach { fragment ->
                        if (fragment is AnalyzeFastFragment) {
                            if (!fragment.analysisStarted) {
                                hubConnection.send("returnState", "analyzeFastFragment")
                            } else {
                                hubConnection.send(
                                    "returnState",
                                    "analyzeFastFragmentAnalysisStarted"
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
            }
        }
        if (currentFragment == R.id.analyzeFragment) {
            try {
                val fm: FragmentManager? = activity?.supportFragmentManager
                fm?.fragments?.forEach {
                    it.childFragmentManager.fragments.forEach { fragment ->
                        if (fragment is AnalyzeFragment) {
                            if (!fragment.analysisStarted) {
                                hubConnection.send("returnState", "analyzeFragment")
                            } else {
                                hubConnection.send(
                                    "returnState",
                                    "analyzeFragmentAnalysisStarted"
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
            }
        }
        if (currentFragment == R.id.sortFragment) {
            try {
                val fm: FragmentManager? = activity?.supportFragmentManager
                fm?.fragments?.forEach {
                    it.childFragmentManager.fragments.forEach { fragment ->
                        if (fragment is SortFragment) {
                            if (!fragment.isSortingStarted.get()) {
                                if(!fragment.initialized.get()){
                                    hubConnection.send("returnState", "sortFragmentOff")
                                }
                                else{
                                    hubConnection.send("returnState", "sortFragmentOn")
                                }
                            } else {
                                if(!fragment.initialized.get()){
                                    hubConnection.send("returnState", "sortFragmentSortingStartedOff")
                                }
                                else{
                                    hubConnection.send("returnState", "sortFragmentSortingStartedOn")
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
            }
        }
        if (currentFragment == R.id.captureFragment) {
            try {
                val fm: FragmentManager? = activity?.supportFragmentManager
                fm?.fragments?.forEach {
                    it.childFragmentManager.fragments.forEach { fragment ->
                        if (fragment is CaptureFragment) {
                            hubConnection.send("returnState", "captureFragment")
                        }
                    }
                }
            } catch (e: Exception) {
            }
        }
        if (currentFragment == R.id.startFragment) {
            try {
                val fm: FragmentManager? = activity?.supportFragmentManager
                fm?.fragments?.forEach {
                    it.childFragmentManager.fragments.forEach { fragment ->
                        if (fragment is StartFragment) {
                            hubConnection.send("returnState", "startFragment")
                        }
                    }
                }
            } catch (e: Exception) {
            }
        }
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