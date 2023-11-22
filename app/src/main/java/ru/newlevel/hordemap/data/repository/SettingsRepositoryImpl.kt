package ru.newlevel.hordemap.data.repository

import ru.newlevel.hordemap.app.mapToDataModel
import ru.newlevel.hordemap.app.mapToDomainModel
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.data.storage.interfaces.UserLocalStorage
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.repository.SettingsRepository

class SettingsRepositoryImpl(private val userLocalStorage: UserLocalStorage): SettingsRepository {

    override fun saveUser(userDomainModel: UserDomainModel) {
        userLocalStorage.save(userDomainModel.mapToDataModel())
        UserEntityProvider.userEntity = userDomainModel
    }

    override fun getUser(): UserDomainModel {
        val user = userLocalStorage.get()
        UserEntityProvider.userEntity = user.mapToDomainModel()
        return user.mapToDomainModel()
    }

    override fun resetUser() {
        userLocalStorage.reset()
    }

    override fun saveAutoLoad(boolean: Boolean) {
        userLocalStorage.saveAutoLoad(boolean)
    }
}