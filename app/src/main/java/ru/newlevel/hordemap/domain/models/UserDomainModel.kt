package ru.newlevel.hordemap.domain.models

import ru.newlevel.hordemap.app.DEFAULT_GROUP
import ru.newlevel.hordemap.app.DEFAULT_SIZE
import ru.newlevel.hordemap.app.DEFAULT_TIME

data class UserDomainModel(
    override var name: String = "Anonymous",
    override var timeToSendData: Int = DEFAULT_TIME,
    override var usersMarkerSize: Int = DEFAULT_SIZE,
    override var staticMarkerSize: Int = DEFAULT_SIZE,
    override var selectedMarker: Int = 0,
    override val deviceID: String = "0",
    override var autoLoad: Boolean = false,
    override val authName: String = "",
    override val profileImageUrl: String = "",
    override val userGroup: Int = DEFAULT_GROUP): UserModel
