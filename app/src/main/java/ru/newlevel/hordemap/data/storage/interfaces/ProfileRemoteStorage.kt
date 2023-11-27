package ru.newlevel.hordemap.data.storage.interfaces

import ru.newlevel.hordemap.data.storage.models.UserDataModel

interface ProfileRemoteStorage {
    suspend fun sendUserData(userData: UserDataModel)

    suspend fun deleteUserDataRemote(deviceId: String)
}