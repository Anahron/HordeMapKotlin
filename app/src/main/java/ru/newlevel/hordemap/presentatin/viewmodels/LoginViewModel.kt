package ru.newlevel.hordemap.presentatin.viewmodels


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.usecases.GetUserUseCase
import ru.newlevel.hordemap.domain.usecases.ResetUserUseCase
import ru.newlevel.hordemap.domain.usecases.SaveUserUseCase

class LoginViewModel(
    private val getUserUseCase: GetUserUseCase,
    private val saveUserUseCase: SaveUserUseCase,
    private val resetUserUseCase: ResetUserUseCase,
) : ViewModel() {

    private val resultLiveDataMutable = MutableLiveData<UserDomainModel>()
    private val loginResultLiveDataMutable = MutableLiveData<UserDomainModel>()
    val loginResult: LiveData<UserDomainModel> = loginResultLiveDataMutable
    val resultData: LiveData<UserDomainModel> = resultLiveDataMutable

    init {
        Log.e("AAA", "LoginVM created")
    }

    fun saveUser(userDomainModel: UserDomainModel) {
        saveUserUseCase.execute(userDomainModel)
        loginSuccess(userDomainModel)
    }

    fun checkLogin(): UserDomainModel {
        val user = getUserUseCase.execute()
        loginResultLiveDataMutable.value = user
        return user
    }

    fun getUser() {
        resultLiveDataMutable.value = getUserUseCase.execute()
    }

    fun reset() {
        resetUserUseCase.execute()
    }

    private fun loginSuccess(userDomainModel: UserDomainModel) {
        loginResultLiveDataMutable.value = userDomainModel
    }
}