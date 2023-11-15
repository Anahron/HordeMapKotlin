package ru.newlevel.hordemap.domain.models

import com.google.android.gms.maps.model.LatLng

data class TrackItemDomainModel(
    val timestamp: Long,
    val title: String = "My track",
    val sessionId: String,
    val date: String,
    val duration: String = "1m",
    val durationLong: Long,
    val distance: String = "0m",
    val distanceMeters: Int,
    val locations: List<LatLng>
)