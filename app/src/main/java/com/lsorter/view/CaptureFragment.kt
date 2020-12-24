package com.lsorter.view

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
import androidx.navigation.fragment.navArgs
import com.lsorter.capture.LegoBrickDatasetCapture
import com.lsorter.capture.RemoteLegoBrickImagesCapture
import com.lsorter.databinding.CaptureFragmentBinding

class CaptureFragment : Fragment() {

    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var binding: CaptureFragmentBinding
    private lateinit var legoBrickImagesCapture: LegoBrickDatasetCapture

    private val args: CaptureFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = CaptureFragmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupCamera()

        if (args.autoCaptureMode) {
            scheduleImagesCapture()
        } else {
            registerCaptureImageOnButtonClick()
        }
    }

    private fun registerCaptureImageOnButtonClick() {
        binding.captureButton.visibility = View.VISIBLE
        binding.captureButton.setOnClickListener {
            binding.captureButton.isClickable = false
            this.legoBrickImagesCapture.captureImage(label = args.legoClassLabel)
            this.legoBrickImagesCapture.setOnImageCapturedListener {
                binding.captureButton.isClickable = true
            }
        }
    }

    private fun scheduleImagesCapture() {
        this.legoBrickImagesCapture.captureImages(
            frequencyMs = args.captureIntervalMs,
            label = args.legoClassLabel
        )
    }

    private fun setupCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this.requireContext())

        cameraProviderFuture.addListener(Runnable {
            this.cameraProvider = cameraProviderFuture.get()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val preview = Preview.Builder().build()

            val imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setFlashMode(ImageCapture.FLASH_MODE_ON)
                .build()

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, preview)
            preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)

            this.legoBrickImagesCapture = RemoteLegoBrickImagesCapture(imageCapture)

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview)
            preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        }, ContextCompat.getMainExecutor(this.requireContext()))
    }

    override fun onDestroy() {
        cameraProvider.unbindAll()
        legoBrickImagesCapture.stop()
        binding.graphicOverlay.clear()
        binding.viewFinder.removeAllViews()
        super.onDestroy()
    }
}