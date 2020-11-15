package com.lsorter

import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.STRATEGY_BLOCK_PRODUCER
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.lsorter.databinding.FragmentPreviewBinding
import com.lsorter.detection.analysis.ImageAnalyzer

class PreviewFragment : Fragment() {
    private lateinit var binding: FragmentPreviewBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPreviewBinding.inflate(inflater, container, false)

        startCamera()

        return binding.root
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this.requireContext())

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val analysisUseCase = ImageAnalysis.Builder()
                .setTargetResolution(QUAD_HD_SIZE)
                .build()
                .also {
                    it.setAnalyzer(
                        ContextCompat.getMainExecutor(this.requireContext()),
                        ImageAnalyzer(binding.graphicOverlay)
                    )
                }

            val preview = Preview.Builder().build()

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, analysisUseCase, preview)
            preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)

        }, ContextCompat.getMainExecutor(this.requireContext()))
    }

    companion object {
        val QUAD_HD_SIZE = Size(2560, 1440)
    }
}