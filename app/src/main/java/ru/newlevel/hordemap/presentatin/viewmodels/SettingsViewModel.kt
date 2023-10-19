package ru.newlevel.hordemap.presentatin.viewmodels


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.usecases.GetUserUseCase
import ru.newlevel.hordemap.domain.usecases.ResetUserUseCase
import ru.newlevel.hordemap.domain.usecases.SaveAutoLoadUseCase
import ru.newlevel.hordemap.domain.usecases.SaveUserUseCase

class SettingsViewModel(
    private val getUserUseCase: GetUserUseCase,
    private val saveUserUseCase: SaveUserUseCase,
    private val resetUserUseCase: ResetUserUseCase,
    private val saveAutoLoadUseCase : SaveAutoLoadUseCase,
) : ViewModel() {

    private val resultLiveDataMutable = MutableLiveData<UserDomainModel>()
    val resultData: LiveData<UserDomainModel> = resultLiveDataMutable

    private val loginResultLiveDataMutable = MutableLiveData<UserDomainModel>()
    val loginResultData: LiveData<UserDomainModel> = loginResultLiveDataMutable

    init {
        Log.e("AAA", "LoginVM created")
    }

    fun saveUser(userDomainModel: UserDomainModel) {
        saveUserUseCase.execute(userDomainModel)
        resultLiveDataMutable.value = userDomainModel
        loginResultLiveDataMutable.value = userDomainModel
    }

    fun saveAutoLoad(boolean: Boolean){
        saveAutoLoadUseCase.execute(boolean)
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

}