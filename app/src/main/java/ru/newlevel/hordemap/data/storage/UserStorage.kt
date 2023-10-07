package ru.newlevel.hordemap.data.storage

import ru.newlevel.hordemap.data.models.UserDataModel

interface UserStorage {
    fun save(user: UserDataModel): Boolean

    fun load(): UserDataModel
}