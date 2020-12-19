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

    private val _eventCaptureStarted = MutableLiveData<Boolean>()
    val eventCaptureStarted
        get() = _eventCaptureStarted

    private val _eventCaptureStopped = MutableLiveData<Boolean>()
    val eventCaptureStopped
        get() = _eventCaptureStopped

    fun onStreamStarted() {
        eventStreamStarted.value = true
    }

    fun onStreamStopped() {
        eventStreamStopped.value = true
    }

    fun onCaptureStarted() {
        eventCaptureStarted.value = true
    }

    fun onCaptureStopped() {
        eventCaptureStopped.value = true
    }
}