package ru.newlevel.hordemap.data.repository

import ru.newlevel.hordemap.data.storage.models.UserDataModel
import ru.newlevel.hordemap.data.storage.UserStorage

import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.repository.UserRepository

class UserRepositoryImpl(private val userStorage: UserStorage): UserRepository {

    override fun saveUser(userDomainModel: UserDomainModel) {
        userStorage.save(mapToData(userDomainModel))
    }

    override fun getUser(): UserDomainModel {
        return mapToDomain(userStorage.get())
    }

    override fun resetUser() {
        userStorage.reset()
    }

    private fun mapToDomain(user: UserDataModel) : UserDomainModel{
        return UserDomainModel(user.name,user.timeToSendData,user.usersMarkerSize,user.staticMarkerSize,user.selectedMarker,user.deviceID)
    }

    private fun mapToData(userDomainModel: UserDomainModel) : UserDataModel{
        return UserDataModel(userDomainModel.name,userDomainModel.timeToSendData,userDomainModel.usersMarkerSize,userDomainModel.staticMarkerSize,userDomainModel.selectedMarker,userDomainModel.deviceID)
    }
}