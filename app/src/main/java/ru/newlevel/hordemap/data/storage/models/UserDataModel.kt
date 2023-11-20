package ru.newlevel.hordemap.data.storage.models

import ru.newlevel.hordemap.domain.models.UserModel

data class UserDataModel(
    override var name: String,
    override var timeToSendData: Int,
    override var usersMarkerSize: Int,
    override var staticMarkerSize: Int,
    override var selectedMarker: Int,
    override val deviceID: String,
    override var autoLoad: Boolean): UserModel
