package ru.newlevel.hordemap.domain.repository

import ru.newlevel.hordemap.domain.models.UserDomainModel

interface SettingsRepository {

    fun saveUser(userDomainModel: UserDomainModel)

    fun getUser(): UserDomainModel

    fun resetUser()

    fun saveAutoLoad(boolean: Boolean)
}