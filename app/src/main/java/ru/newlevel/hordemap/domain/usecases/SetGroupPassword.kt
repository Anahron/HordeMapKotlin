package ru.newlevel.hordemap.domain.usecases

import ru.newlevel.hordemap.domain.repository.GroupsRepository

class SetGroupPassword (private val groupsRepository: GroupsRepository)  {
    suspend fun execute(password: String, userGroup: Int) = groupsRepository.setPasswordForGroup(userGroup = userGroup, password = password)
}