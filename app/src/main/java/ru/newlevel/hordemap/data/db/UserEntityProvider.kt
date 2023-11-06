package ru.newlevel.hordemap.data.db

import ru.newlevel.hordemap.data.storage.models.UserDataModel

object UserEntityProvider {
    var sessionId = 0L
    var userEntity: UserDataModel? = null
}