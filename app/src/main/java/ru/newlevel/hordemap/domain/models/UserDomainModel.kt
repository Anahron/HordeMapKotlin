package ru.newlevel.hordemap.domain.models

class UserDomainModel(
    val name: String,
    val timeToSendData: Int,
    val usersMarkerSize: Int,
    val staticMarkerSize: Int,
    val selectedMarker: Int,
    val deviceID: String,
    var autoLoad: Boolean
) {
}