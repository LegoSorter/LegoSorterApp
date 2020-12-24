package com.lsorter.view

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.lsorter.R
import com.lsorter.capture.RemoteLegoBrickImagesCapture
import com.lsorter.databinding.CaptureFragmentBinding
import com.lsorter.detection.analysis.LegoImageAnalyzer

class CaptureFragment : Fragment() {

    companion object {
        fun newInstance() = CaptureFragment()
    }

    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var binding: CaptureFragmentBinding
    private lateinit var viewModel: CaptureViewModel

    private var legoBrickImagesCapture: RemoteLegoBrickImagesCapture? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = CaptureFragmentBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CaptureViewModel::class.java)

        startCapture()
    }

    private fun startCapture() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this.requireContext())

        cameraProviderFuture.addListener(Runnable {
            this.cameraProvider = cameraProviderFuture.get()
            this.legoBrickImagesCapture = RemoteLegoBrickImagesCapture()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val preview = Preview.Builder().build()

            val imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setFlashMode(ImageCapture.FLASH_MODE_ON)
                .build()

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, preview)
            preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)

            this.legoBrickImagesCapture!!.captureImages(
                imageCapture,
                frequencyMs = PreviewFragment.CAPTURE_FREQUENCY_MS,
                label = "LEGO"
            )
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview)
            preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        }, ContextCompat.getMainExecutor(this.requireContext()))
    }

    override fun onDestroy() {
        cameraProvider.unbindAll()
        legoBrickImagesCapture?.stop()
        binding.graphicOverlay.clear()
        binding.viewFinder.removeAllViews()
        super.onDestroy()
    }
}