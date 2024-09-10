package ru.newlevel.hordemap.domain.repository

import ru.newlevel.hordemap.data.storage.models.UserDataModel
import ru.newlevel.hordemap.presentation.settings.GroupInfoModel

interface GroupsRepository {
    suspend fun getProfilesAndChildCounts(): List<GroupInfoModel>

    suspend fun getProfilesInGroup(groupNumber: Int): List<UserDataModel>
}