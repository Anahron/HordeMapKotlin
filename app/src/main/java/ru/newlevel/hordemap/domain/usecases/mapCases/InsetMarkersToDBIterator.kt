package ru.newlevel.hordemap.domain.usecases.mapCases

import ru.newlevel.hordemap.app.mapToUserEntity
import ru.newlevel.hordemap.data.db.MarkerEntity
import ru.newlevel.hordemap.domain.repository.GeoDataRepository

class InsetMarkersToDBIterator(private val geoDataRepository: GeoDataRepository) {

    suspend fun insertUserMarkers(data: List<MarkerEntity>) {
        geoDataRepository.insertUserMarkers(data.map { it.mapToUserEntity() })
    }

    suspend fun insertStaticMarkers(data: List<MarkerEntity>) {
        geoDataRepository.insertStaticMarkers(data)
    }
}