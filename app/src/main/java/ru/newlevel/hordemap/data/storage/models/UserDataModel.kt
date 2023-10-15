package ru.newlevel.hordemap.data.storage.models

class UserDataModel(
    val name: String,
    val timeToSendData: Int,
    val usersMarkerSize: Int,
    val staticMarkerSize: Int,
    val selectedMarker: Int,
    val deviceID: String,
    var autoLoad: Boolean)
