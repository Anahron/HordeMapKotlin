package ru.newlevel.hordemap.domain.models

import com.google.android.gms.maps.model.LatLng

data class TrackItemDomainModel(
    val timestamp: Long = 0,
    var title: String = "",
    val sessionId: String,
    val date: String,
    val duration: String = "0m",
    val durationLong: Long = 0,
    val distance: String = "0m",
    val distanceMeters: Int = 0,
    val locations: List<LatLng>,
    val isFavourite: Boolean = false
)