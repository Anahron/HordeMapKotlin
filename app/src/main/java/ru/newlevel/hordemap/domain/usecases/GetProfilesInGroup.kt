package ru.newlevel.hordemap.domain.usecases

import ru.newlevel.hordemap.app.mapToDomainModel
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.repository.GroupsRepository

class GetProfilesInGroup(private val groupsRepository: GroupsRepository) {
    suspend fun execute(groupNumber: Int): List<UserDomainModel> {
        val users: ArrayList<UserDomainModel> = ArrayList()
        groupsRepository.getProfilesInGroup(groupNumber)
            .forEach { users.add(it.mapToDomainModel()) }
        return users
    }
}