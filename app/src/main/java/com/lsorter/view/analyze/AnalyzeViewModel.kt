package com.lsorter.view.analyze

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AnalyzeViewModel : ViewModel() {

    private val _eventActionButtonClicked = MutableLiveData<Boolean>()
    val eventActionButtonClicked
        get() = _eventActionButtonClicked

    fun onActionButtonClicked() {
        eventActionButtonClicked.value = true
    }

}