package ru.newlevel.hordemap.presentation.sign_in

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.newlevel.hordemap.app.TAG
import ru.newlevel.hordemap.app.getMyDeviceId
import ru.newlevel.hordemap.domain.usecases.mapCases.GetUserSettingsUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.SaveUserSettingsUseCase
import ru.newlevel.hordemap.presentation.MyResult

class SignInViewModel(
    private val googleAuthUiClient: GoogleAuthUiClient,
    private val saveUserSettingsUseCase: SaveUserSettingsUseCase,
    private val getUserSettingsUseCase: GetUserSettingsUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    fun onSingInResult(result: SingInResult) {
        _state.update {
            Log.e(TAG, "result.data != null = " + (result.data != null).toString())
            it.copy(
                isSingSuccess = result.data != null, signInError = result.errorMessage
            )
        }
    }

    fun saveUser(userData: UserData?, newUserName: String, context: Context) {
        val user = getUserSettingsUseCase.execute()
        val authName = userData?.userName?: "Anonymous"
        val deviceID =  if (authName == "Anonymous") context.getMyDeviceId() else userData?.userId?: context.getMyDeviceId()
        userData?.userId?.let {
            saveUserSettingsUseCase.execute(user.copy(
                authName = authName,
                profileImageUrl = userData.profileImageUrl ?: "",
                deviceID = deviceID,
                name = newUserName
            ))
        }
    }

    fun getSignedInUser(): UserData? = googleAuthUiClient.getSignedInUser()
    suspend fun signInFromIntent(intent: Intent): SingInResult = googleAuthUiClient.signInFromIntent(intent)
    suspend fun signInAnonymously(): SingInResult  = googleAuthUiClient.signInAnonymously()
    suspend fun signIn(): MyResult<*> = googleAuthUiClient.signIn()

}