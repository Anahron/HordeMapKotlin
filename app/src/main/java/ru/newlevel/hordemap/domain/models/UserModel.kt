package ru.newlevel.hordemap.domain.models

interface UserModel {
    var name: String
    var timeToSendData: Int
    var usersMarkerSize: Int
    var staticMarkerSize: Int
    var selectedMarker: Int
    val deviceID: String
    var autoLoad: Boolean
    val authName: String
    val profileImageUrl: String
}