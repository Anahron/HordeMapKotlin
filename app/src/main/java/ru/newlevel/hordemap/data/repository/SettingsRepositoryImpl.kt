package ru.newlevel.hordemap.data.repository

import android.net.Uri
import ru.newlevel.hordemap.data.storage.interfaces.ProfilePhotoStorage
import ru.newlevel.hordemap.data.storage.interfaces.UserLocalStorage
import ru.newlevel.hordemap.data.storage.models.UserDataModel
import ru.newlevel.hordemap.domain.repository.SettingsRepository

class SettingsRepositoryImpl(private val userLocalStorage: UserLocalStorage, private val profilePhotoStorage: ProfilePhotoStorage): SettingsRepository {

    override fun saveUserSetting(userDataModel: UserDataModel) = userLocalStorage.save(userDataModel)

    override fun getUserSetting(): UserDataModel = userLocalStorage.get()

    override fun resetUser() = userLocalStorage.reset()

    override fun saveAutoLoad(boolean: Boolean) = userLocalStorage.saveAutoLoad(boolean)

    override suspend fun uploadProfilePhoto(uri: Uri, fileName: String): Result<Uri> {
       return profilePhotoStorage.uploadProfilePhoto(uri, fileName)
    }
}