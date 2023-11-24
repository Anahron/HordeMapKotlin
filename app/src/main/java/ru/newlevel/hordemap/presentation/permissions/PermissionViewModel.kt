package ru.newlevel.hordemap.presentation.permissions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

sealed class PermissionState {
    object AddLocationPermState : PermissionState()
    object AddBackLocationState : PermissionState()
    object InfoState : PermissionState()
    object AddBackWorking : PermissionState()
}

class PermissionViewModel: ViewModel() {

    private val _state = MutableLiveData<PermissionState>().apply { value =
        PermissionState.InfoState
    }
    val state: LiveData<PermissionState> get() = _state


    fun turnToAddBackWorkingState() {
        _state.value = PermissionState.AddBackWorking
    }

    fun turnToAddLocationState() {
        _state.value = PermissionState.AddLocationPermState
    }

    fun turnToAddBackLocationState() {
        _state.value = PermissionState.AddBackLocationState
    }
}