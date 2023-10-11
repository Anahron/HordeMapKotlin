package ru.newlevel.hordemap.domain.repository

import com.google.android.gms.maps.model.Marker

interface MarkerRepository {

    fun clearSavedUserMarker()

    fun clearSavedStaticMarker()

    fun clearTextMarker()

    fun addSavedUserMarker(marker: Marker)

    fun addSavedStaticMarker(marker: Marker)

    fun addTextMarker(marker: Marker)

    fun getSavedUsersMarkers(): List<Marker>

    fun getSavedStaticMarkers(): List<Marker>

    fun getTextMarkers(): List<Marker>
}