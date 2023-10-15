package ru.newlevel.hordemap.data.storage

import ru.newlevel.hordemap.data.storage.models.UserDataModel


interface UserStorage {
    fun save(userDataModel: UserDataModel)

    fun get(): UserDataModel

    fun reset()

    fun saveAutoLoad(boolean: Boolean)
}

