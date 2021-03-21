package com.lsorter.view.sort

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.common.util.concurrent.ListenableFuture
import com.lsorter.analyze.common.RecognizedLegoBrick
import com.lsorter.analyze.layer.LegoGraphic
import com.lsorter.databinding.FragmentSortBinding
import com.lsorter.sort.DefaultLegoBrickSorterService
import com.lsorter.sort.LegoBrickSorterService
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SortFragment : Fragment() {
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private lateinit var viewModel: SortViewModel
    private lateinit var binding: FragmentSortBinding
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var sorterService: LegoBrickSorterService
    private lateinit var imageCapture: ImageCapture

    private var isSortingStarted = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSortBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(SortViewModel::class.java)

        binding.sortViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        sorterService = DefaultLegoBrickSorterService()

        viewModel.eventStartStopButtonClicked.observe(
            viewLifecycleOwner,
            Observer {
                if (isSortingStarted) {
                    stopSorting()
                } else {
                    startSorting()
                }
            }
        )

        setupCamera()
    }

    private fun startSorting() {
        executor.submit {
            imageCapture.takePicture(
                cameraExecutor,
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        prepareOverlay(image)

                        val recognizedBricks: List<RecognizedLegoBrick> =
                            sorterService.processImage(image)
                        drawBoundingBoxes(recognizedBricks)
                        super.onCaptureSuccess(image)
                    }

                    private fun prepareOverlay(image: ImageProxy) {
                        if (image.imageInfo.rotationDegrees == 90)
                            binding.graphicOverlay.setImageSourceInfo(image.height, image.width)
                        else
                            binding.graphicOverlay.setImageSourceInfo(image.width, image.height)
                    }
                })
        }
    }

    private fun drawBoundingBoxes(recognizedBricks: List<RecognizedLegoBrick>) {
        binding.graphicOverlay.clear()

        for (brick in recognizedBricks) {
            binding.graphicOverlay.add(LegoGraphic(binding.graphicOverlay, brick))
        }

        binding.graphicOverlay.postInvalidate()
    }

    private fun stopSorting() {
        binding.graphicOverlay.let {
            it.clear()
            it.postInvalidate()
        }
        executor.shutdown()
    }

    private fun setupCamera(): ListenableFuture<ProcessCameraProvider> {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this.requireContext())

        cameraProviderFuture.addListener(Runnable {
            this.cameraProvider = cameraProviderFuture.get()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val preview = Preview.Builder().build()

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setFlashMode(ImageCapture.FLASH_MODE_OFF)
                .build()

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, preview)
            preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        }, ContextCompat.getMainExecutor(this.requireContext()))

        return cameraProviderFuture
    }

    override fun onDestroy() {
        cameraProvider.unbindAll()
        stopSorting()

        super.onDestroy()
    }
}