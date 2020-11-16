package com.lsorter.view

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PreviewViewModel : ViewModel() {

    private val _eventStreamStarted = MutableLiveData<Boolean>()
    val eventStreamStarted
        get() = _eventStreamStarted

    private val _eventStreamStopped = MutableLiveData<Boolean>()
    val eventStreamStopped
        get() = _eventStreamStopped

    fun onStreamStarted() {
        eventStreamStarted.value = true
    }

    fun onStreamStopped() {
        eventStreamStopped.value = true
    }
}