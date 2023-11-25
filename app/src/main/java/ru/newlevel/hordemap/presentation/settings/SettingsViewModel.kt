package ru.newlevel.hordemap.presentation.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.usecases.mapCases.GetUserSettingsUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.ResetUserSettingsUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.SaveAutoLoadUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.SaveUserSettingsUseCase

sealed class UiState{
    data object SettingsState: UiState()
    data object LoadMapState : UiState()
}
class SettingsViewModel(
    private val getUserSettingsUseCase: GetUserSettingsUseCase,
    private val saveUserSettingsUseCase: SaveUserSettingsUseCase,
    private val resetUserSettingsUseCase: ResetUserSettingsUseCase,
    private val saveAutoLoadUseCase : SaveAutoLoadUseCase,
) : ViewModel() {
    private val resultLiveDataMutable = MutableLiveData<UserDomainModel>()
    val resultData: LiveData<UserDomainModel> = resultLiveDataMutable

    private val _state = MutableStateFlow<UiState>(UiState.SettingsState)
    val state = _state.asStateFlow()

    fun setState(checkedId: Int) {
        when (checkedId){
            R.id.btnToggleSettings -> _state.value = UiState.SettingsState
            R.id.btnToggleLoadMap -> _state.value = UiState.LoadMapState
        }

    }

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