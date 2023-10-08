package ru.newlevel.hordemap.presentatin

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.newlevel.hordemap.data.repository.UserRepositoryImpl
import ru.newlevel.hordemap.domain.usecases.GetUserUseCase
import ru.newlevel.hordemap.domain.usecases.ResetUserUseCase
import ru.newlevel.hordemap.domain.usecases.SaveUserUseCase

class LoginViewModelFactory(context: Context) : ViewModelProvider.Factory {

    private val userRepository by lazy(LazyThreadSafetyMode.NONE) { UserRepositoryImpl(context = context) }
    private val getUserUseCase by lazy(LazyThreadSafetyMode.NONE) {  GetUserUseCase(userRepository = userRepository)}
    private val saveUserUseCase by lazy(LazyThreadSafetyMode.NONE) { SaveUserUseCase(userRepository = userRepository) }
    private val resetUserUseCase by lazy(LazyThreadSafetyMode.NONE) { ResetUserUseCase(userRepository = userRepository) }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(getUserUseCase = getUserUseCase, saveUserUseCase = saveUserUseCase, resetUserUseCase = resetUserUseCase ) as T
    }
}