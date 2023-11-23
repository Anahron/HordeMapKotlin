package ru.newlevel.hordemap.domain.models

data class UserDomainModel(
    override var name: String,
    override var timeToSendData: Int,
    override var usersMarkerSize: Int,
    override var staticMarkerSize: Int,
    override var selectedMarker: Int,
    override val deviceID: String,
    override var autoLoad: Boolean,
    override val authName: String,
    override val profileImageUrl: String): UserModel
