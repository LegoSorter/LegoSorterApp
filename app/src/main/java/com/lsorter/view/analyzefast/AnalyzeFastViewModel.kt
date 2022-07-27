package com.lsorter.view.analyzefast

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AnalyzeFastViewModel : ViewModel() {

    private val _eventActionButtonClicked = MutableLiveData<Boolean>()
    val eventActionButtonClicked
        get() = _eventActionButtonClicked

    fun onActionButtonClicked() {
        eventActionButtonClicked.value = true
    }

}