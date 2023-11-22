package ru.newlevel.hordemap.domain.repository

import ru.newlevel.hordemap.data.storage.models.UserDataModel

interface SettingsRepository {

    fun saveUser(userDataModel: UserDataModel)

    fun getUser(): UserDataModel

    fun resetUser()

    fun saveAutoLoad(boolean: Boolean)
}