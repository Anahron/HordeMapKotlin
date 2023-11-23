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

    fun saveUser(userDomainModel: UserDomainModel) {
        saveUserSettingsUseCase.execute(userDomainModel)
        resultLiveDataMutable.value = userDomainModel
    }

    fun saveAutoLoad(boolean: Boolean){
        saveAutoLoadUseCase.execute(boolean)
    }

    fun getUserSettings(): UserDomainModel {
        val user = getUserSettingsUseCase.execute()
        resultLiveDataMutable.value = user
        return user

    }

    fun reset() {
        resetUserSettingsUseCase.execute()
    }

}