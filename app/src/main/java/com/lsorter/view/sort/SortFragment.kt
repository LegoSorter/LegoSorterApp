package com.lsorter.view.sort

import android.annotation.SuppressLint
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.Camera2Interop
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
    private var isMachineStarted: AtomicBoolean = AtomicBoolean(false)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSortBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(SortViewModel::class.java)

        binding.sortViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.focusBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val progressScaled = progress.toFloat() / (seekBar?.max ?: 100)
                viewModel.apply {
                    cameraFocusDistance = calculateFocusDistance(progressScaled)
                    binding.focusDistanceValue.text = String.format("%.2f", cameraFocusDistance)
                }

                setupCamera(startAnalysis = false)
            }

            private fun SortViewModel.calculateFocusDistance(progressScaled: Float) =
                maximumCameraFocusDistance + (minimumCameraFocusDistance - maximumCameraFocusDistance) * progressScaled

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        sorterService = DefaultLegoBrickSorterService()

        viewModel.eventStartStopSortingButtonClicked.observe(
            viewLifecycleOwner,
            Observer {
                if (isSortingStarted.get()) {
                    isSortingStarted.set(false)
                    setVisibilityOfFocusSeeker(View.VISIBLE)
                    stopSorting()
                    binding.startStopSortingButton.text = getString(com.lsorter.R.string.start_sorting_text)
                } else {
                    setVisibilityOfFocusSeeker(View.GONE)
                    startSorting()
                    binding.startStopSortingButton.text = getString(com.lsorter.R.string.stop_sorting_text)
                    isSortingStarted.set(true)
                }
            }
        )

        viewModel.eventStartStopMachineButtonClicked.observe(
            viewLifecycleOwner,
            Observer {
                if(isMachineStarted.get()) {
                    isMachineStarted.set(false)
                    binding.startStopMachineButton.text = getString(com.lsorter.R.string.start_machine_text)
                } else {
                    isMachineStarted.set(true)
                    binding.startStopMachineButton.text = getString(com.lsorter.R.string.stop_machine_text)
                }
            }
        )

        setupCamera()
    }

    private fun setVisibilityOfFocusSeeker(visibility: Int) {
        binding.focusBar.visibility = visibility
        binding.focusBarLabel.visibility = visibility
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
            val preview = Preview.Builder().apply { setFocusDistance(this) }.build()

            if (startAnalysis) {
                val imageAnalysis = getImageAnalysis()
                cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview)
            } else {
                cameraProvider.bindToLifecycle(this, cameraSelector, preview)
            }.apply {
                extractLensCharacteristics()
            }

            preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        }, ContextCompat.getMainExecutor(this.requireContext()))

        return cameraProviderFuture
    }

    @SuppressLint("UnsafeExperimentalUsageError", "RestrictedApi")
    private fun Camera.extractLensCharacteristics() {
        Camera2CameraInfo.extractCameraCharacteristics(this.cameraInfo).apply {
            this@SortFragment.viewModel.minimumCameraFocusDistance =
                this.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE)
                    ?: 0f

            this@SortFragment.viewModel.maximumCameraFocusDistance =
                this.get(CameraCharacteristics.LENS_INFO_HYPERFOCAL_DISTANCE)
                    ?: Float.MAX_VALUE
        }
    }

    private fun getImageAnalysis(): ImageAnalysis {
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            //                    .setTargetResolution(Size(1080, 1920))
            .build()
            .also {
                it.setAnalyzer(
                    cameraExecutor,
                    ImageAnalysis.Analyzer { image ->
                        binding.graphicOverlay.setImageSourceInfo(
                            image.width,
                            image.height,
                            image.imageInfo.rotationDegrees
                        )

                        sorterService.processImage(image).apply {
                            drawBoundingBoxes(this)
                        }
                        image.close()
                    })
            }
        return imageAnalysis
    }

    private fun setFocusDistance(previewBuilder: Preview.Builder) {
        Camera2Interop.Extender(previewBuilder).apply {
            this.setCaptureRequestOption(
                CaptureRequest.CONTROL_AF_MODE,
                CameraMetadata.CONTROL_AF_MODE_OFF
            )
            this.setCaptureRequestOption(
                CaptureRequest.LENS_FOCUS_DISTANCE,
                viewModel.cameraFocusDistance
            )
        }
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