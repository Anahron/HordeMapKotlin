package ru.newlevel.hordemap.presentation.sign_in

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.getMyDeviceId
import ru.newlevel.hordemap.domain.usecases.SendUserToStorageUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.GetUserSettingsUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.SaveUserSettingsUseCase

class SignInViewModel(
    private val googleAuthUiClient: GoogleAuthUiClient,
    private val saveUserSettingsUseCase: SaveUserSettingsUseCase,
    private val getUserSettingsUseCase: GetUserSettingsUseCase,
    private val sendUserToStorageUseCase: SendUserToStorageUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    fun onSingInResult(result: SingInResult) {
        _state.update {
            it.copy(
                isSingSuccess = result.data != null, signInError = result.errorMessage
            )
        }
    }

    suspend fun saveUser(userData: UserData?, newUserName: String, context: Context) {
        val user = getUserSettingsUseCase.execute()
        val authName = userData?.userName ?: context.getString(R.string.hintAnonim)
        val newDeviceID =
            if (userData?.userName != null)
                userData.userId
            else
                context.getMyDeviceId()
        val userPhoto = if (user.profileImageUrl.isEmpty()) userData?.profileImageUrl?: "" else ""
        val newUser = user.copy(
            authName = authName,
            profileImageUrl = userPhoto,
            deviceID = newDeviceID,
            name = newUserName,
        )
        userData?.userId.let {
            saveUserSettingsUseCase.execute(newUser)
            sendUserToStorageUseCase.execute(newUser)
        }
    }

    suspend fun getSignedInUser(): UserData? = googleAuthUiClient.getSignedInUser()
    suspend fun signInFromIntent(intent: Intent): SingInResult = googleAuthUiClient.signInFromIntent(intent)
    suspend fun signInAnonymously(): SingInResult = googleAuthUiClient.signInAnonymously()
    suspend fun signIn(): MyResult<*> = googleAuthUiClient.signIn()

}