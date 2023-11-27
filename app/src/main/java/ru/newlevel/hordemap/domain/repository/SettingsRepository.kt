package ru.newlevel.hordemap.domain.repository

import android.net.Uri
import ru.newlevel.hordemap.data.storage.models.UserDataModel

interface SettingsRepository {

    fun saveUserSetting(userDataModel: UserDataModel)

    suspend fun sendUserToStorage(userDataModel: UserDataModel)

    fun getUserSetting(): UserDataModel

    fun resetUserSettings()

    suspend fun deleteUserDataRemote(deviceId: String)
    fun saveAutoLoad(boolean: Boolean)

    suspend fun uploadProfilePhoto(uri: Uri, fileName: String): Result<Uri>
}