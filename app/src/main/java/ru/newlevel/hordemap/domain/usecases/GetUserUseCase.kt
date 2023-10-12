package ru.newlevel.hordemap.domain.usecases

import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.data.storage.models.UserDataModel
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.repository.UserRepository

class GetUserUseCase(private val userRepository: UserRepository) {
    fun execute(): UserDomainModel {
        val user = userRepository.getUser()
        UserEntityProvider.userEntity = UserDataModel(user.name,user.timeToSendData,user.usersMarkerSize,user.staticMarkerSize,user.selectedMarker,user.deviceID)
        return userRepository.getUser()
    }
}