package com.lsorter.view

import android.annotation.SuppressLint
import android.hardware.camera2.CaptureRequest
import android.os.Bundle
import android.util.Range
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.lsorter.databinding.FragmentPreviewBinding
import com.lsorter.detection.analysis.LegoImageAnalyzer
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class PreviewFragment : Fragment() {
    private val analysisExecutor: Executor = Executors.newFixedThreadPool(4)
    private lateinit var binding: FragmentPreviewBinding
    private lateinit var viewModel: PreviewViewModel

    private var isRecordingStarted = false

    private var cameraProvider: ProcessCameraProvider? = null
    private var legoImageAnalyzer: LegoImageAnalyzer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPreviewBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(PreviewViewModel::class.java)
        binding.previewViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        prepareCamera()

        viewModel.eventActionButtonClicked.observe(
            viewLifecycleOwner,
            Observer { eventActionButtonClicked ->
                if (eventActionButtonClicked) {
                    if (!isRecordingStarted) {
                        binding.capOrAnalyse.isEnabled = false
                        if (!binding.capOrAnalyse.isChecked) {
                            analyzeImages()
                        } else {
                            stopCamera()
                            findNavController().navigate(PreviewFragmentDirections.actionPreviewFragmentToCaptureDialogFragment())
                        }

                        binding.startstop.text = "STOP"
                        isRecordingStarted = true
                    } else {
                        binding.capOrAnalyse.isEnabled = true
                        if (!binding.capOrAnalyse.isChecked) {
                            stopImageAnalysis()
                        }

                        binding.startstop.text = "START"
                        isRecordingStarted = false
                    }
                }
            })

        return binding.root
    }

    private fun prepareCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this.requireContext())

        cameraProviderFuture.addListener(Runnable {
            this.cameraProvider = cameraProviderFuture.get()
            this.legoImageAnalyzer = LegoImageAnalyzer(binding.graphicOverlay)
            val cameraProvider = cameraProvider!!
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val preview = Preview.Builder().build()

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview)
            preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        }, ContextCompat.getMainExecutor(this.requireContext()))
    }

    private fun analyzeImages() {
        this.legoImageAnalyzer = LegoImageAnalyzer(binding.graphicOverlay)
        val imageAnalyzer = legoImageAnalyzer!!
        val cameraProvider = cameraProvider!!
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        val builder = ImageAnalysis.Builder()
        val extender = Camera2Interop.Extender(builder)
        extender.setCaptureRequestOption(
            CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,
            Range(10, 10)
        )

        val analysisUseCase = builder
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(
                    analysisExecutor,
                    imageAnalyzer
                )
            }

        val preview = Preview.Builder().build()

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(this, cameraSelector, analysisUseCase, preview)
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