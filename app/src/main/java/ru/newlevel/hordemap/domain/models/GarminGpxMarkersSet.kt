package ru.newlevel.hordemap.domain.models

import com.google.android.gms.maps.model.LatLngBounds

data class GarminGpxMarkersSet(
    val markers: List<GarminMarkerModel>,
    val bounds: LatLngBounds?
)
