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
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.lsorter.databinding.FragmentPreviewBinding
import com.lsorter.detection.analysis.ImageAnalyzer

class PreviewFragment : Fragment() {
    private lateinit var binding: FragmentPreviewBinding
    private lateinit var viewModel: PreviewViewModel

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalyzer: ImageAnalyzer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPreviewBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(PreviewViewModel::class.java)
        binding.previewViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.eventStreamStarted.observe(viewLifecycleOwner, Observer { eventStreamStarted ->
            if (eventStreamStarted) {
                binding.startButton.visibility = View.GONE
                startCamera()
                binding.stopButton.visibility = View.VISIBLE
            }
        })

        viewModel.eventStreamStopped.observe(viewLifecycleOwner, Observer { eventStreamStopped ->
            if (eventStreamStopped) {
                binding.stopButton.visibility = View.GONE
                stopCamera()
                binding.startButton.visibility = View.VISIBLE
            }
        })

        return binding.root
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this.requireContext())

        cameraProviderFuture.addListener(Runnable {
            this.cameraProvider = cameraProviderFuture.get()
            this.imageAnalyzer = ImageAnalyzer(binding.graphicOverlay)
            val imageAnalyzer = imageAnalyzer!!
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
        }, ContextCompat.getMainExecutor(this.requireContext()))
    }

    @SuppressLint("RestrictedApi")
    private fun stopCamera() {
        cameraProvider?.unbindAll()
        imageAnalyzer?.shutdown()

        binding.graphicOverlay.clear()
        binding.viewFinder.removeAllViews()
        binding.invalidateAll()
    }

    companion object {
        val QUAD_HD_SIZE = Size(2560, 1440)
    }
}