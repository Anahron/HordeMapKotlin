package ru.newlevel.hordemap.data.storage.models

import ru.newlevel.hordemap.domain.models.UserModel

data class UserDataModel @JvmOverloads constructor(
    override var name: String,
    override var timeToSendData: Int = 60,
    override var usersMarkerSize: Int = 60,
    override var staticMarkerSize: Int = 60,
    override var selectedMarker: Int = 0,
    override val deviceID: String,
    override var autoLoad: Boolean = false,
    override val authName: String,
    override val profileImageUrl: String) : UserModel
