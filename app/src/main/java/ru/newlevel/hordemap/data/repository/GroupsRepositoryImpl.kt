package ru.newlevel.hordemap.data.repository

import ru.newlevel.hordemap.data.storage.interfaces.ProfileRemoteStorage
import ru.newlevel.hordemap.data.storage.models.UserDataModel
import ru.newlevel.hordemap.domain.repository.GroupsRepository
import ru.newlevel.hordemap.presentation.settings.GroupInfoModel

class GroupsRepositoryImpl(private val profileRemoteStorage: ProfileRemoteStorage): GroupsRepository {
    override suspend fun  getProfilesAndChildCounts(): List<GroupInfoModel> = profileRemoteStorage.getProfilesAndChildCounts()
    override suspend fun getProfilesInGroup(groupNumber: Int): List<UserDataModel>  = profileRemoteStorage.getProfilesInGroup(groupNumber = groupNumber)
}