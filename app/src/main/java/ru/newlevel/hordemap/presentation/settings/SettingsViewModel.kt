package ru.newlevel.hordemap.presentation.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.usecases.mapCases.GetUserSettingsUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.ResetUserSettingsUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.SaveAutoLoadUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.SaveUserSettingsUseCase

class SettingsViewModel(
    private val getUserSettingsUseCase: GetUserSettingsUseCase,
    private val saveUserSettingsUseCase: SaveUserSettingsUseCase,
    private val resetUserSettingsUseCase: ResetUserSettingsUseCase,
    private val saveAutoLoadUseCase : SaveAutoLoadUseCase,
) : ViewModel() {
    private val resultLiveDataMutable = MutableLiveData<UserDomainModel>()
    val resultData: LiveData<UserDomainModel> = resultLiveDataMutable

    private val loginResultLiveDataMutable = MutableLiveData<UserDomainModel>()
    val loginResultData: LiveData<UserDomainModel> = loginResultLiveDataMutable

    fun saveUser(userDomainModel: UserDomainModel) {
        saveUserSettingsUseCase.execute(userDomainModel)
        resultLiveDataMutable.value = userDomainModel
        loginResultLiveDataMutable.value = userDomainModel
    }

    fun saveAutoLoad(boolean: Boolean){
        saveAutoLoadUseCase.execute(boolean)
    }

    fun checkLogin(): UserDomainModel {
        val user = getUserSettingsUseCase.execute()
        loginResultLiveDataMutable.value = user
        return user
    }

    fun getUser() {
        resultLiveDataMutable.value = getUserSettingsUseCase.execute()
    }

    fun reset() {
        resetUserSettingsUseCase.execute()
    }

}