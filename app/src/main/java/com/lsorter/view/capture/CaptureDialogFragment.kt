package com.lsorter.view.capture

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.lsorter.databinding.FragmentCaptureDialogBinding


class CaptureDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return FragmentCaptureDialogBinding.inflate(inflater, container, false).root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val binding = FragmentCaptureDialogBinding.inflate(layoutInflater)

            builder
                .setView(binding.root)
                .setMessage("Configure capture preferences")
                .setPositiveButton("Start") { _, _ ->
                    findNavController().navigate(
                        CaptureDialogFragmentDirections.actionCaptureDialogFragmentToCaptureFragment(
                            legoClassLabel = binding.legoClassLabel.text.toString(),
                            captureIntervalMs = calculateInterval(binding.seekBar.progress),
                            autoCaptureMode = binding.automaticModeSwitch.isChecked
                        )
                    )
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }

            binding.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                @SuppressLint("SetTextI18n")
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    binding.currentSeekBarValue.text = "${calculateInterval(i)} ms"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })

            binding.automaticModeSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    binding.captureIntervalSetting.visibility = View.VISIBLE
                } else {
                    binding.captureIntervalSetting.visibility = View.GONE
                }
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun calculateInterval(progressValue: Int): Int {
        return progressValue * SEEK_BAR_CAPTURE_INTERVAL_STEP_MS + SEEK_BAR_CAPTURE_MIN_VALUE_MS
    }

    companion object {
        const val SEEK_BAR_CAPTURE_INTERVAL_STEP_MS: Int = 250
        const val SEEK_BAR_CAPTURE_MIN_VALUE_MS: Int = 500
    }
}