package com.lsorter.view

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.lsorter.R
import com.lsorter.databinding.FragmentStartBinding

class StartFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentStartBinding.inflate(inflater, container, false);

        binding.startButton.setOnClickListener(
            Navigation.createNavigateOnClickListener(
                StartFragmentDirections.actionStartFragmentToPreviewFragment()
            )
        )
        val savedAddr = activity?.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE)?.getString(
            getString(R.string.saved_server_address_key),
            "ip:port") ?: "ip:port"
        binding.serverAddressBox.setText(savedAddr)
        binding.serverAddressBox.setOnFocusChangeListener {_, hasFocus ->
            if (!hasFocus) {
                val sharedPref = activity?.getSharedPreferences(
                    getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                if (sharedPref != null) {
                    with (sharedPref.edit()) {
                        putString(getString(R.string.saved_server_address_key), binding.serverAddressBox.text.toString())
                        apply()
                    }
                }
            }
        }

        return binding.root
    }
}