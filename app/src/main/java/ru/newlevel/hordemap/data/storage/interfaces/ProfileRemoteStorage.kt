package ru.newlevel.hordemap.data.storage.interfaces

import ru.newlevel.hordemap.data.storage.models.UserDataModel

interface ProfileRemoteStorage {
    suspend fun sendUserData(userData: UserDataModel)
}