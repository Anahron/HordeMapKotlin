package ru.newlevel.hordemap.data.db

import ru.newlevel.hordemap.domain.models.UserDomainModel

object UserEntityProvider {
    var sessionId = 0L
    var userEntity: UserDomainModel = UserDomainModel()
}