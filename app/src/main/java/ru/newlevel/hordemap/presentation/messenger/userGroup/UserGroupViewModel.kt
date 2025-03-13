package ru.newlevel.hordemap.presentation.messenger.userGroup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.usecases.GetGroupPassword
import ru.newlevel.hordemap.domain.usecases.SetGroupPassword
import ru.newlevel.hordemap.domain.usecases.messengerCases.MessengerUseCases
import kotlin.String

class UserGroupViewModel(
    messengerUseCases: MessengerUseCases,
    private val getGroupPassword: GetGroupPassword,
    private val setGroupPassword: SetGroupPassword
) : ViewModel() {

    private val _usersProfileDataFlow: Flow<List<UserDomainModel>> =
        messengerUseCases.messageUpdateInteractor.getUsersProfiles()
    val usersProfileDataFlow get(): Flow<List<UserDomainModel>> = _usersProfileDataFlow

    private val _lockState: MutableStateFlow<String> = MutableStateFlow("")
    val lockState get(): StateFlow<String> = _lockState

    suspend fun getGroupPass(userGroup: Int) {
        try {
            // Асинхронный запрос в фоновом потоке
            val password = withContext(Dispatchers.IO) {
                getGroupPassword.execute(userGroup)  // Получаем пароль
            }

            // Обновляем состояние
            _lockState.value = password
        } catch (e: Exception) {
            // Логируем ошибку и передаем пустую строку или сообщение об ошибке
            Log.e("TAG", "Failed to retrieve password: ${e.message}")
            _lockState.value = ""  // Можно установить состояние ошибки
        }
    }
    fun setPassword(password: String) {
        viewModelScope.launch {
            setGroupPassword.execute(password = password, userGroup = UserEntityProvider.userEntity.userGroup)
        }
    }
}
