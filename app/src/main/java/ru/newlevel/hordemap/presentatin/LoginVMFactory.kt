package ru.newlevel.hordemap.presentatin

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.newlevel.hordemap.data.repository.UserRepositoryImpl
import ru.newlevel.hordemap.domain.usecases.GetUserUseCase
import ru.newlevel.hordemap.domain.usecases.SaveUserUseCase

class LoginVMFactory(context: Context) : ViewModelProvider.Factory {

    private val userRepository by lazy(LazyThreadSafetyMode.NONE) { UserRepositoryImpl(context = context) }
    private val getUserUseCase by lazy(LazyThreadSafetyMode.NONE) {  GetUserUseCase(userRepository = userRepository)}
    private val saveUserUseCase by lazy(LazyThreadSafetyMode.NONE) { SaveUserUseCase(userRepository = userRepository) }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginVM(getUserUseCase = getUserUseCase, saveUserUseCase = saveUserUseCase) as T
    }
}