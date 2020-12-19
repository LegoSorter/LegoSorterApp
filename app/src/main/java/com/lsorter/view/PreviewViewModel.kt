package com.lsorter.view

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PreviewViewModel : ViewModel() {

    private val _eventActionButtonClicked = MutableLiveData<Boolean>()
    val eventActionButtonClicked
        get() = _eventActionButtonClicked

    fun onActionButtonClicked() {
        eventActionButtonClicked.value = true
    }

}