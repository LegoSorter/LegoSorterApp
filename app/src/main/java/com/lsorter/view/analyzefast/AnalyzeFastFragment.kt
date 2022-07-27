package com.lsorter.view.analyzefast

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.lsorter.R
import com.lsorter.analyze.LegoImageAnalyzerFast
import com.lsorter.databinding.FragmentAnalyzeFastBinding
import com.lsorter.utils.PreferencesUtils
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class AnalyzeFastFragment : Fragment() {
    private val analysisExecutor: Executor = Executors.newFixedThreadPool(4)
    private lateinit var binding: FragmentAnalyzeFastBinding
    private lateinit var viewModel: AnalyzeFastViewModel
    private var analysisStarted = false
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
                        binding.cameraSwitch.isEnabled = false;
                        binding.webSwitch.isEnabled = false;
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
                        binding.cameraSwitch.isEnabled = true;
                        binding.webSwitch.isEnabled = true;
                        analysisStarted = false;
//                        binding.webViewFast.visibility = View.INVISIBLE;
//                        binding.webViewFast.loadUrl("");
//                        binding.webViewFast.
                    }
                }
            })
        val savedCameraPrev = activity?.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )?.getBoolean("user_camera_prev",true)?:true
        val savedWebPrev = activity?.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )?.getBoolean("user_web_prev",false)?:false

        if(binding.cameraSwitch.isChecked&& binding.webSwitch.isChecked){
            binding.webSwitch.isChecked=false;
        }

        binding.cameraSwitch.setOnCheckedChangeListener(){ _, checked ->

            val sharedPref = activity?.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE
            )
            if (sharedPref != null) {
                with(sharedPref.edit()) {
                    putBoolean("user_camera_prev",checked);
                    if(checked && binding.webSwitch.isChecked){
                        binding.webSwitch.isChecked=false;
                    }
                    apply()
                }
            }

        }
        binding.cameraSwitch.isChecked = savedCameraPrev;

        binding.webSwitch.setOnCheckedChangeListener { _, checked ->

            val sharedPref = activity?.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE
            )
            if (sharedPref != null) {
                with(sharedPref.edit()) {
                    putBoolean("user_web_prev",checked);
                    if(binding.cameraSwitch.isChecked && checked){
                        binding.cameraSwitch.isChecked=false;
                    }
                    apply()
                }
            }
            if(checked){
                binding.webViewFast.visibility = View.VISIBLE;
                binding.webViewFast.settings.javaScriptEnabled = true;
                binding.webViewFast.settings.domStorageEnabled = true;
                binding.webViewFast.webViewClient = WebViewClient()
                binding.webViewFast.loadUrl("http://192.168.11.189/rawbelt");
            }
            else{
                binding.webViewFast.visibility = View.INVISIBLE;
                binding.webViewFast.loadUrl("");
            }
        }
        binding.webSwitch.isChecked = savedWebPrev; //set after Listener to update view with value

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        prepareCamera()
    }

    private fun prepareCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this.requireContext())

        cameraProviderFuture.addListener(Runnable {
            this.cameraProvider = cameraProviderFuture.get()
            this.legoImageAnalyzerFast =
                LegoImageAnalyzerFast(binding.graphicOverlayFast)
            val cameraProvider = cameraProvider!!
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val preview = Preview.Builder().build()

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview)
            if(binding.cameraSwitch.isChecked)
                preview.setSurfaceProvider(binding.viewFinderFast.surfaceProvider)// set where to display camera feed
        }, ContextCompat.getMainExecutor(this.requireContext()))
    }

    private fun analyzeImages() {
        this.legoImageAnalyzerFast =
            LegoImageAnalyzerFast(binding.graphicOverlayFast)
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

        val preview = PreferencesUtils.extendPreviewView(Preview.Builder(), context)
            .build()

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(this, cameraSelector, analysisUseCase, preview)
            .apply {
                PreferencesUtils.applyPreferences(this, context)
            }
        if(binding.cameraSwitch.isChecked)
            preview.setSurfaceProvider(binding.viewFinderFast.surfaceProvider)
    }

    private fun stopImageAnalysis() {
        stopCamera()
        prepareCamera()
    }

    @SuppressLint("RestrictedApi")
    private fun stopCamera() {
        cameraProvider?.unbindAll()
        legoImageAnalyzerFast?.shutdown()

        binding.graphicOverlayFast.clear()
        binding.viewFinderFast.removeAllViews()
        binding.invalidateAll()
    }
}