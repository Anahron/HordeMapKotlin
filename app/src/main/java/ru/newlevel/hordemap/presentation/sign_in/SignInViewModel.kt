package ru.newlevel.hordemap.presentation.sign_in

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.newlevel.hordemap.app.TAG
import ru.newlevel.hordemap.domain.usecases.mapCases.GetUserSettingsUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.SaveUserSettingsUseCase

class SignInViewModel(private val saveUserSettingsUseCase: SaveUserSettingsUseCase, private val getUserSettingsUseCase: GetUserSettingsUseCase): ViewModel() {
    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    fun onSingInResult(result: SingInResult) {
        _state.update {
            Log.e(TAG, "result.data != null = " +(result.data != null).toString())
            it.copy(
                isSingSuccess = result.data != null, signInError = result.errorMessage
            )
        }

    }
    fun saveUser(userData: UserData?){
        val user = getUserSettingsUseCase.execute()
        userData?.userId?.let {
            user.copy(
                authName = userData.userName?: "",
                profileImageUrl = userData.profileImageUrl?: "",
                deviceID = it
            )
        }
        saveUserSettingsUseCase.execute(user)
    }
    fun resetState() {
        _state.update {
            SignInState()
        }
    }
}