package ru.newlevel.hordemap.data.repository

import com.google.android.gms.maps.model.Marker
import ru.newlevel.hordemap.domain.repository.MarkerRepository

class MarkerRepositoryImpl: MarkerRepository {
    private val savedUsersMarkers: ArrayList<Marker> = ArrayList()
    private val savedStaticMarkers: ArrayList<Marker> = ArrayList()
    private val savedTextMarkers: ArrayList<Marker> = ArrayList()

    @Synchronized
    override fun clearSavedUserMarker() {
        savedUsersMarkers.clear()
    }
    @Synchronized
    override fun clearSavedStaticMarker() {
        savedStaticMarkers.clear()
    }
    @Synchronized
    override fun clearTextMarker() {
        savedTextMarkers.clear()
    }

    @Synchronized
    override fun addSavedUserMarker(marker: Marker) {
        savedUsersMarkers.add(marker)
    }
    @Synchronized
    override fun addSavedStaticMarker(marker: Marker) {
        savedStaticMarkers.add(marker)
    }
    @Synchronized
    override fun addTextMarker(marker: Marker) {
        savedTextMarkers.add(marker)
    }
    @Synchronized
    override fun getSavedUsersMarkers(): List<Marker> {
        return savedUsersMarkers
    }
    @Synchronized
    override fun getSavedStaticMarkers(): List<Marker> {
        return savedStaticMarkers
    }
    @Synchronized
    override fun getTextMarkers(): List<Marker> {
        return savedTextMarkers
    }

    override fun hideMarkers() {
        for (marker in savedStaticMarkers)
            marker.isVisible = false
        for (marker in savedUsersMarkers)
            marker.isVisible = false
        for (marker in savedTextMarkers)
            marker.isVisible = false
    }

    override fun showMarkers() {
        for (marker in savedStaticMarkers)
            marker.isVisible = true
        for (marker in savedUsersMarkers)
            marker.isVisible = true
        for (marker in savedTextMarkers)
            marker.isVisible = true
    }
}