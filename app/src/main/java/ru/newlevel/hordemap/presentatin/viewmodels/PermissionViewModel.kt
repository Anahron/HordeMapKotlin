package ru.newlevel.hordemap.presentatin.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.newlevel.hordemap.domain.usecases.GetUserUseCase
import ru.newlevel.hordemap.domain.usecases.SaveUserUseCase

sealed class PermissionState {
    class AddLocationPermState : PermissionState()
    class AddBackLocationState : PermissionState()
    class AddUserNameState : PermissionState()
    class InfoState : PermissionState()
    class AddBackWorking : PermissionState()
}

class PermissionViewModel(
    private val getUserUseCase: GetUserUseCase,
    private val saveUserUseCase: SaveUserUseCase
) : ViewModel() {

    private val _state = MutableLiveData<PermissionState>().apply { value = PermissionState.InfoState() }
    val state: LiveData<PermissionState> get() = _state

    fun checkUserName(): Boolean {
        return getUserUseCase.execute().name.length > 2
    }

    fun saveUserName(string: String) {
        val user = getUserUseCase.execute()
        user.name = string
        saveUserUseCase.execute(user)
    }

    fun turnToAddBackWorkingState() {
        _state.value = PermissionState.AddBackWorking()
    }

    fun turnToAddLocationState() {
        _state.value = PermissionState.AddLocationPermState()
    }

    fun turnToAddBackLocationState() {
        _state.value = PermissionState.AddBackLocationState()
    }

    fun turnToAddUserNameState() {
        _state.value = PermissionState.AddUserNameState()
    }
}