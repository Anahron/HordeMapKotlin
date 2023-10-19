package ru.newlevel.hordemap.domain.models

class UserDomainModel(
    var name: String,
    var timeToSendData: Int,
    var usersMarkerSize: Int,
    var staticMarkerSize: Int,
    var selectedMarker: Int,
    val deviceID: String,
    var autoLoad: Boolean
) {
}