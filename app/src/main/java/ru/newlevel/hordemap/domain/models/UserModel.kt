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
    val userGroup: Int
    val lastSeen: Long
    val showRuler: Boolean
    val showCoordinates: Boolean
    val showGaussCoordinates: Boolean
    val showCompass: Boolean
    val zoomByVolume: Boolean
}