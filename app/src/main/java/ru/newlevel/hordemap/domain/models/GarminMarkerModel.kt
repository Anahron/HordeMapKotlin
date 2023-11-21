package ru.newlevel.hordemap.domain.models

import com.google.android.gms.maps.model.LatLng

data class GarminMarkerModel(
    val name: String = "",
    val latLng: LatLng,
    val markerType: String = "",
    val markerColor: String = "",
)