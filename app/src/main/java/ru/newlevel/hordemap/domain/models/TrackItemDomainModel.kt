package ru.newlevel.hordemap.domain.models

import com.google.android.gms.maps.model.LatLng

data class TrackItemDomainModel(
    val title: String = "My track",
    val date: String,
    val duration: String = "1m",
    val distance: String = "0m",
    val locations: List<LatLng>
)