package ru.newlevel.hordemap.presentation.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.data.storage.models.MapFileModel
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.usecases.SendUserToStorageUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.GetAllMapsAsListUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.GetUserSettingsUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.LoadProfilePhotoUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.ResetUserSettingsUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.SaveAutoLoadUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.SaveUserSettingsUseCase

sealed class UiState {
    data object SettingsState : UiState()
    data object LoadMapState : UiState()
}

class SettingsViewModel(
    private val getUserSettingsUseCase: GetUserSettingsUseCase,
    private val saveUserSettingsUseCase: SaveUserSettingsUseCase,
    private val resetUserSettingsUseCase: ResetUserSettingsUseCase,
    private val saveAutoLoadUseCase: SaveAutoLoadUseCase,
    private val loadProfilePhotoUseCase: LoadProfilePhotoUseCase,
    private val sendUserToStorageUseCase: SendUserToStorageUseCase,
    private val getAllMapsAsListUseCase: GetAllMapsAsListUseCase,
) : ViewModel() {
    private val resultLiveDataMutable = MutableLiveData<UserDomainModel>()
    val resultData: LiveData<UserDomainModel> = resultLiveDataMutable

    private val _mapsList = MutableLiveData<List<MapFileModel>>()
    val mapsList : LiveData<List<MapFileModel>> = _mapsList

    private val _state = MutableStateFlow<UiState>(UiState.SettingsState)
    val state = _state.asStateFlow()

    fun setState(checkedId: Int) {
        when (checkedId) {
            R.id.btnToggleSettings -> _state.value = UiState.SettingsState
            R.id.btnToggleLoadMap -> _state.value = UiState.LoadMapState
        }

    }

    suspend fun getAllMapsAsList(): Boolean {
        val result = getAllMapsAsListUseCase.execute()
        result.onSuccess {
            _mapsList.postValue(it)
        }
        return true
    }

    suspend fun loadProfilePhoto(uri: Uri, context: Context): Result<Uri> {
        val result = loadProfilePhotoUseCase.execute(uri, context)
        result.onSuccess {
            val user = getUserSettings()
            saveUser(
                user.copy(
                    profileImageUrl = it.toString()
                )
            )
        }
        return result
    }

    suspend fun saveUser(userDomainModel: UserDomainModel) {
        UserEntityProvider.userEntity = userDomainModel
        sendUserToStorageUseCase.execute(userDomainModel)
        saveUserSettingsUseCase.execute(userDomainModel)
        resultLiveDataMutable.value = userDomainModel
    }

    fun saveAutoLoad(boolean: Boolean) {
        saveAutoLoadUseCase.execute(boolean)
    }

    fun getUserSettings(): UserDomainModel {
        val user = getUserSettingsUseCase.execute()
        resultLiveDataMutable.value = user
        return user

    }

    fun reset() {
        resetUserSettingsUseCase.execute()
        resultLiveDataMutable.value = getUserSettingsUseCase.execute()
    }
}