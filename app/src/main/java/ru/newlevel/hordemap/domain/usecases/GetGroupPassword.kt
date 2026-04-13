package ru.newlevel.hordemap.domain.usecases

import ru.newlevel.hordemap.domain.repository.GroupsRepository

class GetGroupPassword(private val groupsRepository: GroupsRepository)  {
    suspend fun execute(userGroup: Int): String = groupsRepository.getPasswordForGroup(userGroup = userGroup)
}