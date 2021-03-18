package com.lsorter.view.sort

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SortViewModel : ViewModel() {

    private val _eventStartStopButtonClicked = MutableLiveData<Boolean>()
    val eventStartStopButtonClicked
        get() = _eventStartStopButtonClicked

    fun onStartStop() {
        eventStartStopButtonClicked.value = true
    }
}