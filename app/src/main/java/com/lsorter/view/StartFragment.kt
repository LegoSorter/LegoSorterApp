package com.lsorter.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
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

        return binding.root
    }
}