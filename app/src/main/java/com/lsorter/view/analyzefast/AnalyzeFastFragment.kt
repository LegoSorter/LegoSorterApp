package com.lsorter.view.analyzefast

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.lsorter.App
import com.lsorter.R
import com.lsorter.analyze.LegoImageAnalyzerFast
import com.lsorter.databinding.FragmentAnalyzeFastBinding
import com.lsorter.utils.PreferencesUtils
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class AnalyzeFastFragment : Fragment() {
    private val analysisExecutor: Executor = Executors.newFixedThreadPool(4)
    public lateinit var binding: FragmentAnalyzeFastBinding
    private lateinit var viewModel: AnalyzeFastViewModel
    public var analysisStarted = false
    private var cameraProvider: ProcessCameraProvider? = null
    private var legoImageAnalyzerFast: LegoImageAnalyzerFast? = null

    //    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAnalyzeFastBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(AnalyzeFastViewModel::class.java)
        binding.analyzeFastViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.eventActionButtonClicked.observe(
            viewLifecycleOwner,
            Observer { eventActionButtonClicked ->
                if (eventActionButtonClicked) {
                    if (!analysisStarted) {
                        analyzeImages()
                        binding.startStopSortingButton.text = getString(R.string.stop_text)
                        binding.saveImgSwitch.isEnabled = false;
                        binding.sessionName.isEnabled = false;
//                        binding.cameraSwitch.isEnabled = false;
//                        binding.webSwitch.isEnabled = false;
                        analysisStarted = true;
//                        binding.webViewFast.visibility = View.VISIBLE;
//                        binding.webViewFast.settings.javaScriptEnabled = true;
////                        binding.webViewFast.settings.allowContentAccess = true;
//                        binding.webViewFast.settings.domStorageEnabled = true;
//                        binding.webViewFast.webViewClient = WebViewClient()
//                        binding.webViewFast.loadUrl("http://192.168.11.189/rawbelt");
                    } else {
                        stopImageAnalysis()
                        binding.startStopSortingButton.text = getString(R.string.start_text)
                        binding.saveImgSwitch.isEnabled = true;
                        binding.sessionName.isEnabled = true;
//                        binding.cameraSwitch.isEnabled = true;
//                        binding.webSwitch.isEnabled = true;
                        analysisStarted = false;
//                        binding.webViewFast.visibility = View.INVISIBLE;
//                        binding.webViewFast.loadUrl("");
//                        binding.webViewFast.
                    }
                }
            })
//        val savedCameraPrev = activity?.getSharedPreferences(
//            getString(R.string.preference_file_key), Context.MODE_PRIVATE
//        )?.getBoolean("user_camera_prev",true)?:true
        val savedWebPrev = activity?.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )?.getBoolean(getString(R.string.saved_user_web_prev_key), false) ?: false
        if (savedWebPrev) {
            var prefs = PreferenceManager.getDefaultSharedPreferences(App.applicationContext());
            var beltSpeed = prefs.getString("RENDER_BELT_SPEED", "1.0") ?: "1.0"
            var opacity = prefs.getString("RENDER_OPACITY", "75") ?: "75"
            var cameraview = if (prefs.getBoolean("CAMERA_VIEW", true)) "1" else "0"
            val addrPref = App.sharedPreferences().getString(
                App.applicationContext().getString(R.string.saved_web_server_address_key),
                "http://sorter.ml"
            ) ?: "http://sorter.ml" //  http://ip:port
            binding.webViewFast.visibility = View.VISIBLE;
            binding.webViewFast.settings.javaScriptEnabled = true;
            binding.webViewFast.settings.domStorageEnabled = true;
            binding.webViewFast.webViewClient = WebViewClient()
            binding.webViewFast.loadUrl("${addrPref}/rawbelt?speed=${beltSpeed}&opacity=${opacity}&camera=${cameraview}");
//          binding.webViewFast.loadUrl("http://192.168.11.189:5002/rawbelt?speed=${beltSpeed}&opacity=${opacity}&camera=${cameraview}");

            binding.webViewFast.setBackgroundColor(0)
//            binding.webViewFast.loadUrl("http://sorter.ml/rawbelt/${beltSpeed}");
        } else {
            binding.webViewFast.visibility = View.INVISIBLE;
            binding.webViewFast.loadUrl("");
        }
//        if(binding.cameraSwitch.isChecked&& binding.webSwitch.isChecked){
//            binding.webSwitch.isChecked=false;
//        }
//
//        binding.cameraSwitch.setOnCheckedChangeListener(){ _, checked ->
//
//            val sharedPref = activity?.getSharedPreferences(
//                getString(R.string.preference_file_key), Context.MODE_PRIVATE
//            )
//            if (sharedPref != null) {
//                with(sharedPref.edit()) {
//                    putBoolean("user_camera_prev",checked);
//                    if(checked && binding.webSwitch.isChecked){
//                        binding.webSwitch.isChecked=false;
//                    }
//                    apply()
//                }
//            }
//
//        }
//        binding.cameraSwitch.isChecked = savedCameraPrev;
        val saveImgSwitchVal = activity?.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )?.getBoolean(getString(R.string.saved_image_switch_key), false) ?: false
        binding.saveImgSwitch.isChecked = saveImgSwitchVal;

        binding.saveImgSwitch.setOnCheckedChangeListener() { _, checked ->

            val sharedPref = activity?.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE
            )
            if (sharedPref != null) {
                with(sharedPref.edit()) {
                    putBoolean(getString(R.string.saved_image_switch_key), checked);
                    apply()
                }
            }

        }

        binding.webSwitch.setOnCheckedChangeListener { _, checked ->

            val sharedPref = activity?.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE
            )
            if (sharedPref != null) {
                with(sharedPref.edit()) {
                    putBoolean(getString(R.string.saved_user_web_prev_key), checked);
//                    if(binding.cameraSwitch.isChecked && checked){
//                        binding.cameraSwitch.isChecked=false;
//                    }
                    apply()
                }
            }
            if (checked) {
                var prefs = PreferenceManager.getDefaultSharedPreferences(App.applicationContext());
                var beltSpeed = prefs.getString("RENDER_BELT_SPEED", "1.0") ?: "1.0"
                var opacity = prefs.getString("RENDER_OPACITY", "75") ?: "75"
                var cameraview = if (prefs.getBoolean("CAMERA_VIEW", true)) "1" else "0"
                val addrPref = App.sharedPreferences().getString(
                    App.applicationContext().getString(R.string.saved_web_server_address_key),
                    "http://sorter.ml"
                ) ?: "http://sorter.ml" //  http://ip:port
                binding.webViewFast.visibility = View.VISIBLE;
                binding.webViewFast.settings.javaScriptEnabled = true;
                binding.webViewFast.settings.domStorageEnabled = true;
                binding.webViewFast.webViewClient = WebViewClient()
                binding.webViewFast.loadUrl("${addrPref}/rawbelt?speed=${beltSpeed}&opacity=${opacity}&camera=${cameraview}");
//                binding.webViewFast.loadUrl("http://192.168.11.189:5002/rawbelt?speed=${beltSpeed}&opacity=${opacity}&camera=${cameraview}");
                binding.webViewFast.setBackgroundColor(0)
            } else {
                binding.webViewFast.visibility = View.INVISIBLE;
                binding.webViewFast.loadUrl("");
            }
        }
        binding.webSwitch.isChecked = savedWebPrev; //set after Listener to update view with value


        val savedSession = activity?.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )?.getString(
            getString(R.string.saved_session_value_key),
            "Session_1"
        ) ?: "Session_1"
        binding.sessionName.setText(savedSession)

        binding.sessionName.doOnTextChanged { text, start, before, count ->
            val sharedPref = activity?.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE
            )
            if (sharedPref != null) {
                with(sharedPref.edit()) {
                    putString(
                        getString(R.string.saved_session_value_key),
                        text.toString()
                    )
                    apply()
                }
            }
        }

        binding.sessionName.setOnKeyListener { view, i, keyEvent ->
            when {
                //Check if it is the Enter-Key,      Check if the Enter Key was pressed down
                ((keyEvent.action == KeyEvent.ACTION_DOWN) && (i == KeyEvent.KEYCODE_ENTER)) -> {

                    val imm: InputMethodManager? =
                        activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                    imm?.hideSoftInputFromWindow(view?.getWindowToken(), 0)

                    val sharedPref = activity?.getSharedPreferences(
                        getString(com.lsorter.R.string.preference_file_key), Context.MODE_PRIVATE
                    )
                    if (sharedPref != null) {
                        with(sharedPref.edit()) {
                            putString(
                                getString(com.lsorter.R.string.saved_session_value_key),
                                binding.sessionName.text.toString()
                            )
                            apply()
                        }
                    }
                    binding.sessionName.clearFocus()
                    return@setOnKeyListener true
                }
                else -> false
            }

        }


        binding.sessionName.setOnFocusChangeListener { _, hasFocus ->
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
                            getString(R.string.saved_session_value_key),
                            binding.sessionName.text.toString()
                        )
                        apply()
                    }
                }
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        prepareCamera()
    }

    public fun prepareCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this.requireContext())

        cameraProviderFuture.addListener(Runnable {
            val savedSession = activity?.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE
            )?.getString(
                getString(R.string.saved_session_value_key),
                "Session_1"
            ) ?: "Session_1"
            this.cameraProvider = cameraProviderFuture.get()
            this.legoImageAnalyzerFast =
                LegoImageAnalyzerFast(binding.graphicOverlayFast,savedSession)
            val cameraProvider = cameraProvider!!
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//            val preview = Preview.Builder().build()
            val preview = PreferencesUtils.extendPreviewView(
                Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9), context
            )
                .build()

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview)
//            if(binding.cameraSwitch.isChecked)
            preview.setSurfaceProvider(binding.viewFinderFast.surfaceProvider)// set where to display camera feed
        }, ContextCompat.getMainExecutor(this.requireContext()))
    }

    public fun analyzeImages() {
        var savedSession = activity?.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )?.getString(getString(R.string.saved_session_value_key),"Session_1") ?: "Session_1"
        val saveImgSwitchVal = activity?.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )?.getBoolean(getString(R.string.saved_image_switch_key), false) ?: false
        if(!saveImgSwitchVal){
            savedSession=""
        }
        this.legoImageAnalyzerFast =
            LegoImageAnalyzerFast(binding.graphicOverlayFast,savedSession)
        val imageAnalyzer = legoImageAnalyzerFast!!
        val cameraProvider = cameraProvider!!
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        val analysisUseCase =
            PreferencesUtils.extendImageAnalysis(ImageAnalysis.Builder(), context)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(
                        analysisExecutor,
                        imageAnalyzer
                    )
                }

        val preview = PreferencesUtils.extendPreviewView(
            Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9), context
        )
            .build()

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(this, cameraSelector, analysisUseCase, preview)
            .apply {
                PreferencesUtils.applyPreferences(this, context)
            }
//        if(binding.cameraSwitch.isChecked)
        preview.setSurfaceProvider(binding.viewFinderFast.surfaceProvider)
    }

    public fun stopImageAnalysis() {
        stopCamera()
        prepareCamera()
    }

    @SuppressLint("RestrictedApi")
    public fun stopCamera() {
        cameraProvider?.unbindAll()
        legoImageAnalyzerFast?.shutdown()

        binding.graphicOverlayFast.clear()
        binding.viewFinderFast.removeAllViews()
        binding.invalidateAll()
    }
}