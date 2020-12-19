package com.lsorter.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.lsorter.capture.LegoBrickDatasetCapture
import com.lsorter.capture.RemoteLegoBrickImagesCapture
import com.lsorter.connection.ConnectionManager
import com.lsorter.databinding.FragmentPreviewBinding
import com.lsorter.detection.analysis.LegoImageAnalyzer

class PreviewFragment : Fragment() {
    private lateinit var binding: FragmentPreviewBinding
    private lateinit var viewModel: PreviewViewModel

    private var cameraProvider: ProcessCameraProvider? = null
    private var legoImageAnalyzer: LegoImageAnalyzer? = null
    private var legoBrickImagesCapture: LegoBrickDatasetCapture? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPreviewBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(PreviewViewModel::class.java)
        binding.previewViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        prepareCamera()

        viewModel.eventStreamStarted.observe(viewLifecycleOwner, Observer { eventStreamStarted ->
            if (eventStreamStarted) {
                binding.startButton.visibility = View.GONE
                binding.startCaptureButton.visibility = View.GONE
                analyzeImages()
                binding.stopButton.visibility = View.VISIBLE
            }
        })

        viewModel.eventStreamStopped.observe(viewLifecycleOwner, Observer { eventStreamStopped ->
            if (eventStreamStopped) {
                binding.stopButton.visibility = View.GONE
                stopImageAnalysis()
                binding.startButton.visibility = View.VISIBLE
                binding.startCaptureButton.visibility = View.VISIBLE
            }
        })

        viewModel.eventCaptureStarted.observe(viewLifecycleOwner, Observer { eventCaptureStarted ->
            if (eventCaptureStarted) {
                binding.startCaptureButton.visibility = View.GONE
                binding.startButton.visibility = View.GONE
                captureImages()
                binding.stopCaptureButton.visibility = View.VISIBLE
            }
        })

        viewModel.eventCaptureStopped.observe(viewLifecycleOwner, Observer { eventCaptureStopped ->
            if (eventCaptureStopped) {
                binding.stopCaptureButton.visibility = View.GONE
                stopCaptureImages()
                binding.startCaptureButton.visibility = View.VISIBLE
                binding.startButton.visibility = View.VISIBLE
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

        val analysisUseCase = ImageAnalysis.Builder()
            .setTargetResolution(QUAD_HD_SIZE)
            .build()
            .also {
                it.setAnalyzer(
                    ContextCompat.getMainExecutor(this.requireContext()),
                    imageAnalyzer
                )
            }

        val preview = Preview.Builder().build()

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(this, cameraSelector, analysisUseCase, preview)
        preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)
    }

    private fun captureImages() {
        val cameraProvider = cameraProvider!!
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        this.legoBrickImagesCapture = RemoteLegoBrickImagesCapture(ConnectionManager())

        val imageCapture = ImageCapture.Builder()
                .setTargetResolution(QUAD_HD_SIZE)
                .build()

        val preview = Preview.Builder().build()

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, preview)
        preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)

        this.legoBrickImagesCapture!!.captureImages(imageCapture, frequencyMs = CAPTURE_FREQUENCY_MS)
    }

    private fun stopImageAnalysis() {
        stopCamera()
        prepareCamera()
    }

    private fun stopCaptureImages() {
        stopCamera()
        prepareCamera()
    }

    @SuppressLint("RestrictedApi")
    private fun stopCamera() {
        cameraProvider?.unbindAll()
        legoImageAnalyzer?.shutdown()
        legoBrickImagesCapture?.stop()

        binding.graphicOverlay.clear()
        binding.viewFinder.removeAllViews()
        binding.invalidateAll()
    }

    companion object {
        val QUAD_HD_SIZE = Size(1440, 2560)
        val CAPTURE_FREQUENCY_MS: Int = 200
    }
}