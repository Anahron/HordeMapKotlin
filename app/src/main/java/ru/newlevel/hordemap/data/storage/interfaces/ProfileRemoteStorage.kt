package ru.newlevel.hordemap.data.storage.interfaces

import ru.newlevel.hordemap.data.storage.models.UserDataModel
import ru.newlevel.hordemap.presentation.settings.GroupInfoModel

interface ProfileRemoteStorage {
    suspend fun sendUserData(userData: UserDataModel)

    suspend fun deleteUserDataRemote(deviceId: String)
    suspend fun deleteUserDataRemote(deviceId: String,  userGroup: Int)

    suspend fun getProfilesInGroup(groupNumber: Int): List<UserDataModel>
    suspend fun getPasswordForGroup(userGroup: Int): String
    suspend fun setPasswordForGroup(userGroup: Int, password: String)

    suspend fun getProfilesAndChildCounts(): List<GroupInfoModel>
}