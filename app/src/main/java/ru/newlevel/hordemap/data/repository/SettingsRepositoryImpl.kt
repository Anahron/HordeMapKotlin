package ru.newlevel.hordemap.data.repository

import ru.newlevel.hordemap.app.mapUserDataToDomain
import ru.newlevel.hordemap.app.mapUserDomainToData
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.data.storage.models.UserDataModel
import ru.newlevel.hordemap.data.storage.UserStorage
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.repository.SettingsRepository

class SettingsRepositoryImpl(private val userStorage: UserStorage): SettingsRepository {

    override fun saveUser(userDomainModel: UserDomainModel) {
        val user = mapUserDomainToData(userDomainModel)
        userStorage.save(mapUserDomainToData(userDomainModel))
        UserEntityProvider.userEntity = UserDataModel(user.name,user.timeToSendData,user.usersMarkerSize,user.staticMarkerSize,user.selectedMarker,user.deviceID, user.autoLoad)
    }

    override fun getUser(): UserDomainModel {
        val user = userStorage.get()
        UserEntityProvider.userEntity = UserDataModel(user.name,user.timeToSendData,user.usersMarkerSize,user.staticMarkerSize,user.selectedMarker,user.deviceID, user.autoLoad)
        return mapUserDataToDomain(user)
    }

    override fun resetUser() {
        userStorage.reset()
    }

    override fun saveAutoLoad(boolean: Boolean) {
        userStorage.saveAutoLoad(boolean)
    }
}