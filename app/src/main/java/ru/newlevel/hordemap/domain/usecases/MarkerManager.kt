package ru.newlevel.hordemap.domain.usecases

import com.google.android.gms.maps.model.Marker

object  MarkerManager {
    private val savedUsersMarkers: ArrayList<Marker> = ArrayList()
    private val savedStaticMarkers: ArrayList<Marker> = ArrayList()
    private val savedTextMarkers: ArrayList<Marker> = ArrayList()

    @Synchronized
    fun clearSavedUserMarker() {
        savedUsersMarkers.clear()
    }
    @Synchronized
    fun clearSavedStaticMarker() {
        savedStaticMarkers.clear()
    }
    @Synchronized
    fun clearTextMarker() {
        savedTextMarkers.clear()
    }

    @Synchronized
    fun addSavedUserMarker(marker: Marker) {
        savedUsersMarkers.add(marker)
    }
    @Synchronized
    fun addSavedStaticMarker(marker: Marker) {
        savedStaticMarkers.add(marker)
    }
    @Synchronized
    fun addTextMarker(marker: Marker) {
        savedTextMarkers.add(marker)
    }
    @Synchronized
    fun getSavedUsersMarkers(): List<Marker> {
        return savedUsersMarkers
    }
    @Synchronized
    fun getSavedStaticMarkers(): List<Marker> {
        return savedStaticMarkers
    }
    @Synchronized
    fun getTextMarkers(): List<Marker> {
        return savedTextMarkers
    }
}