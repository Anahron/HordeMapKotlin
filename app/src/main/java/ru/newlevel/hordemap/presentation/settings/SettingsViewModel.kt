package ru.newlevel.hordemap.presentation.settings

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.usecases.mapCases.GetSettingsUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.ResetSettingsUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.SaveAutoLoadUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.SaveSettingsUseCase

class SettingsViewModel(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val saveSettingsUseCase: SaveSettingsUseCase,
    private val resetSettingsUseCase: ResetSettingsUseCase,
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
        saveSettingsUseCase.execute(userDomainModel)
        resultLiveDataMutable.value = userDomainModel
        loginResultLiveDataMutable.value = userDomainModel
    }

    fun saveAutoLoad(boolean: Boolean){
        saveAutoLoadUseCase.execute(boolean)
    }

    fun checkLogin(): UserDomainModel {
        val user = getSettingsUseCase.execute()
        loginResultLiveDataMutable.value = user
        return user
    }
}