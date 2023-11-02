package ru.newlevel.hordemap.data.storage.interfaces

import ru.newlevel.hordemap.data.storage.models.UserDataModel


interface UserLocalStorage {
    fun save(userDataModel: UserDataModel)

    fun get(): UserDataModel

    fun reset()

    fun saveAutoLoad(boolean: Boolean)
}

