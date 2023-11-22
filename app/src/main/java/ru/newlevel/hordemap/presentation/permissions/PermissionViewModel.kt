package ru.newlevel.hordemap.presentation.permissions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.newlevel.hordemap.domain.usecases.mapCases.GetUserSettingsUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.SaveUserSettingsUseCase

sealed class PermissionState {
    object AddLocationPermState : PermissionState()
    object AddBackLocationState : PermissionState()
    object AddUserNameState : PermissionState()
    object InfoState : PermissionState()
    object AddBackWorking : PermissionState()
}

class PermissionViewModel(
    private val getUserSettingsUseCase: GetUserSettingsUseCase,
    private val saveUserSettingsUseCase: SaveUserSettingsUseCase
) : ViewModel() {

    private val _state = MutableLiveData<PermissionState>().apply { value =
        PermissionState.InfoState
    }
    val state: LiveData<PermissionState> get() = _state

    fun checkUserName(): Boolean {
        return getUserSettingsUseCase.execute().name.length > 2
    }

    fun saveUserName(string: String) {
        val user = getUserSettingsUseCase.execute()
        user.name = string
        saveUserSettingsUseCase.execute(user)
    }

    fun turnToAddBackWorkingState() {
        _state.value = PermissionState.AddBackWorking
    }

    fun turnToAddLocationState() {
        _state.value = PermissionState.AddLocationPermState
    }

    fun turnToAddBackLocationState() {
        _state.value = PermissionState.AddBackLocationState
    }

    fun turnToAddUserNameState() {
        _state.value = PermissionState.AddUserNameState
    }
}