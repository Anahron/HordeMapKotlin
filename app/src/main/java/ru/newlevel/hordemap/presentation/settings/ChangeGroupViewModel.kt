package ru.newlevel.hordemap.presentation.settings

import androidx.lifecycle.ViewModel
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.usecases.GetProfilesInGroup
import ru.newlevel.hordemap.domain.usecases.GetUsersProfiles

class ChangeGroupViewModel(private val getUsersProfiles: GetUsersProfiles, private val getProfilesInGroup: GetProfilesInGroup): ViewModel() {

    suspend fun getUsersInGroup(groupNumber: Int): List<UserDomainModel>{
      return getProfilesInGroup.execute(groupNumber = groupNumber)
    }

    suspend fun getGroups(): List<GroupInfoModel> {
      return  getUsersProfiles.execute()
    }
}