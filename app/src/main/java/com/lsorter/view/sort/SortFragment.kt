package com.lsorter.view.sort

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
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
import java.util.concurrent.atomic.AtomicBoolean

class SortFragment : Fragment() {

    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private lateinit var viewModel: SortViewModel
    private lateinit var binding: FragmentSortBinding
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var sorterService: LegoBrickSorterService

    private var isSortingStarted: AtomicBoolean = AtomicBoolean(false)

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
                if (isSortingStarted.get()) {
                    isSortingStarted.set(false)
                    stopSorting()
                    binding.startstop.text = "Start"
                } else {
                    startSorting()
                    binding.startstop.text = "Stop"
                    isSortingStarted.set(true)
                }
            }
        )

        setupCamera()
    }

    private fun startSorting() {
        setupCamera(startAnalysis = true)
    }

    private fun stopSorting() {
        cameraProvider.unbindAll()
        binding.graphicOverlay.let {
            it.clear()
            it.postInvalidate()
        }
        setupCamera(startAnalysis = false)
    }

    private fun setupCamera(startAnalysis: Boolean = false): ListenableFuture<ProcessCameraProvider> {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this.requireContext())

        cameraProviderFuture.addListener(Runnable {
            this.cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val preview = Preview.Builder().build()

            if (startAnalysis) {
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                    .setTargetResolution(Size(1080, 1920))
                    .build()
                    .also {
                        it.setAnalyzer(
                            cameraExecutor,
                            ImageAnalysis.Analyzer { image ->
                                if (isSortingStarted.get()) {
                                    if (image.imageInfo.rotationDegrees == 90)
                                        binding.graphicOverlay.setImageSourceInfo(
                                            image.height,
                                            image.width
                                        )
                                    else
                                        binding.graphicOverlay.setImageSourceInfo(
                                            image.width,
                                            image.height
                                        )

                                    sorterService.processImage(image).apply {
                                        drawBoundingBoxes(this)
                                    }
                                }
                                image.close()
                            })
                    }

                cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview)
            } else {
                cameraProvider.bindToLifecycle(this, cameraSelector, preview)
            }

            preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        }, ContextCompat.getMainExecutor(this.requireContext()))

        return cameraProviderFuture
    }

    private fun drawBoundingBoxes(recognizedBricks: List<RecognizedLegoBrick>) {
        binding.graphicOverlay.clear()

        if (isSortingStarted.get()) {

            for (brick in recognizedBricks) {
                binding.graphicOverlay.add(LegoGraphic(binding.graphicOverlay, brick))
            }

            binding.graphicOverlay.postInvalidate()
        }
    }

    override fun onDestroy() {
        stopSorting()

        super.onDestroy()
    }
}