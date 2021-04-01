package com.lsorter.view.analyze

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.lsorter.R
import com.lsorter.databinding.FragmentAnalyzeBinding
import com.lsorter.analyze.LegoImageAnalyzer
import com.lsorter.utils.PreferencesUtils
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class AnalyzeFragment : Fragment() {
    private val analysisExecutor: Executor = Executors.newFixedThreadPool(4)
    private lateinit var binding: FragmentAnalyzeBinding
    private lateinit var viewModel: AnalyzeViewModel
    private var analysisStarted = false
    private var cameraProvider: ProcessCameraProvider? = null
    private var legoImageAnalyzer: LegoImageAnalyzer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAnalyzeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(AnalyzeViewModel::class.java)
        binding.analyzeViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.eventActionButtonClicked.observe(
            viewLifecycleOwner,
            Observer { eventActionButtonClicked ->
                if (eventActionButtonClicked) {
                    if (!analysisStarted) {
                        analyzeImages()
                        binding.startStopSortingButton.text = getString(R.string.stop_text)
                        analysisStarted = true
                    } else {
                        stopImageAnalysis()
                        binding.startStopSortingButton.text = getString(R.string.start_text)
                        analysisStarted = false
                    }
                }
            })

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
            this.legoImageAnalyzer =
                LegoImageAnalyzer(binding.graphicOverlay)
            val cameraProvider = cameraProvider!!
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val preview = Preview.Builder().build()

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview)
            preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        }, ContextCompat.getMainExecutor(this.requireContext()))
    }

    private fun analyzeImages() {
        this.legoImageAnalyzer =
            LegoImageAnalyzer(binding.graphicOverlay)
        val imageAnalyzer = legoImageAnalyzer!!
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
        preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)
    }

    private fun stopImageAnalysis() {
        stopCamera()
        prepareCamera()
    }

    @SuppressLint("RestrictedApi")
    private fun stopCamera() {
        cameraProvider?.unbindAll()
        legoImageAnalyzer?.shutdown()

        binding.graphicOverlay.clear()
        binding.viewFinder.removeAllViews()
        binding.invalidateAll()
    }
}