package ru.newlevel.hordemap.data.repository

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.newlevel.hordemap.data.storage.interfaces.ProfilePhotoStorage
import ru.newlevel.hordemap.data.storage.interfaces.ProfileRemoteStorage
import ru.newlevel.hordemap.data.storage.interfaces.UserLocalStorage
import ru.newlevel.hordemap.data.storage.models.UserDataModel
import ru.newlevel.hordemap.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val userLocalStorage: UserLocalStorage,
    private val profilePhotoStorage: ProfilePhotoStorage,
    private val profileRemoteStorage: ProfileRemoteStorage
) : SettingsRepository {

    override fun saveUserSetting(userDataModel: UserDataModel) = userLocalStorage.save(userDataModel)

    override suspend fun sendUserToStorage(userDataModel: UserDataModel) = withContext(Dispatchers.IO) {
        profileRemoteStorage.sendUserData(userDataModel)
    }

    override fun getUserSetting(): UserDataModel = userLocalStorage.get()

    override fun resetUserSettings() = userLocalStorage.reset()
    override suspend fun deleteUserDataRemote(deviceId: String) =
       profileRemoteStorage.deleteUserDataRemote(deviceId)


    override fun saveAutoLoad(boolean: Boolean) = userLocalStorage.saveAutoLoad(boolean)

    override suspend fun uploadProfilePhoto(uri: Uri, fileName: String): Result<Uri> {
        return profilePhotoStorage.uploadProfilePhoto(uri, fileName)
    }
}