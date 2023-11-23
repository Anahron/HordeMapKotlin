package ru.newlevel.hordemap.domain.repository

import ru.newlevel.hordemap.data.storage.models.UserDataModel

interface SettingsRepository {

    fun saveUserSetting(userDataModel: UserDataModel)

    fun getUserSetting(): UserDataModel

    fun resetUser()

    fun saveAutoLoad(boolean: Boolean)
}