package com.lsorter.view.sort

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SortViewModel : ViewModel() {

    private val _eventStartStopMachineButtonClicked = MutableLiveData<Boolean>()
    val eventStartStopMachineButtonClicked
        get() = _eventStartStopMachineButtonClicked

    private val _eventStartStopButtonClicked = MutableLiveData<Boolean>()
    val eventStartStopSortingButtonClicked
        get() = _eventStartStopButtonClicked

    var cameraFocusDistance: Float = 0f
    var minimumCameraFocusDistance: Float = FOCUS_DISTANCE_MINIMUM_VALUE
        get() = field.coerceAtMost(FOCUS_DISTANCE_MINIMUM_VALUE)

    var maximumCameraFocusDistance: Float = FOCUS_DISTANCE_MAXIMUM_VALUE
        get() = field.coerceAtLeast(FOCUS_DISTANCE_MAXIMUM_VALUE)

    fun onStartStopSorting() {
        eventStartStopSortingButtonClicked.value =
            !(eventStartStopSortingButtonClicked.value ?: false)
    }

    fun onStartStopMachine() {
        eventStartStopMachineButtonClicked.value =
            !(eventStartStopMachineButtonClicked.value ?: false)
    }

    companion object {
        const val FOCUS_DISTANCE_MINIMUM_VALUE: Float = 14f
        const val FOCUS_DISTANCE_MAXIMUM_VALUE: Float = 2f
    }
}