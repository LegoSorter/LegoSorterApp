package com.lsorter.view.capture

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.navArgs
import com.google.common.util.concurrent.ListenableFuture
import com.lsorter.capture.LegoBrickDatasetCapture
import com.lsorter.capture.RemoteLegoBrickImagesCapture
import com.lsorter.databinding.FragmentCaptureBinding
import com.lsorter.utils.PreferencesUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CaptureFragment : Fragment() {

    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var binding: FragmentCaptureBinding
    private lateinit var legoBrickImagesCapture: LegoBrickDatasetCapture

    private val args: CaptureFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCaptureBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        val setupCameraFuture = setupCamera()
        setupCameraFuture.addListener(
            Runnable {
                binding.flashButton.setOnClickListener { button ->
                    button.isActivated = !button.isActivated

                    this.legoBrickImagesCapture.setFlash(button.isActivated)
                }

                if (args.autoCaptureMode) {
                    scheduleImagesCapture()
                } else {
                    registerCaptureImageOnButtonClick()
                }
            },
            ContextCompat.getMainExecutor(this.requireContext())
        )
    }

    private fun registerCaptureImageOnButtonClick() {
        binding.captureButton.visibility = View.VISIBLE

        this.legoBrickImagesCapture.setOnImageCapturedListener {
            activity?.runOnUiThread {
                binding.captureButton.isClickable = true

                val makeText = Toast.makeText(
                    this.requireContext(),
                    "Photo captured",
                    Toast.LENGTH_SHORT
                )
                GlobalScope.launch {
                    makeText.show()
                    delay(500)
                    makeText.cancel()
                }
            }
        }

        binding.captureButton.setOnClickListener {
            binding.captureButton.isClickable = false
            this.legoBrickImagesCapture.captureImage(label = args.legoClassLabel)
        }

    }

    private fun scheduleImagesCapture() {
        this.legoBrickImagesCapture.setOnImageCapturedListener {
            activity?.runOnUiThread {
                val makeText = Toast.makeText(
                    this.requireContext(),
                    "Photo captured",
                    Toast.LENGTH_SHORT
                )
                GlobalScope.launch {
                    makeText.show()
                    delay(500)
                    makeText.cancel()
                }
            }
        }

        this.legoBrickImagesCapture.captureImages(
            frequencyMs = args.captureIntervalMs,
            label = args.legoClassLabel
        )
    }

    private fun setupCamera(): ListenableFuture<ProcessCameraProvider> {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this.requireContext())

        cameraProviderFuture.addListener(Runnable {
            this.cameraProvider = cameraProviderFuture.get()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val preview = PreferencesUtils.extendPreviewView(Preview.Builder(), context)
                .build()
            val imageCapture =
                PreferencesUtils.extendImageCapture(ImageCapture.Builder(), context)
                    .build()

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, preview)
                .apply { PreferencesUtils.applyPreferences(this, context) }

            preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)

            this.legoBrickImagesCapture = RemoteLegoBrickImagesCapture(imageCapture)
        }, ContextCompat.getMainExecutor(this.requireContext()))

        return cameraProviderFuture
    }

    override fun onDestroy() {
        legoBrickImagesCapture.stop()
        cameraProvider.unbindAll()
        binding.viewFinder.removeAllViews()
        super.onDestroy()
    }
}