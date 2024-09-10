package ru.newlevel.hordemap.domain.usecases


import ru.newlevel.hordemap.domain.repository.GroupsRepository
import ru.newlevel.hordemap.presentation.settings.GroupInfoModel

class GetUsersProfiles(private val groupsRepository: GroupsRepository) {
    suspend fun execute(): List<GroupInfoModel> {
      return  groupsRepository.getProfilesAndChildCounts()
    }
}