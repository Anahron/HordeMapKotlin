package ru.newlevel.hordemap.domain.usecases

import ru.newlevel.hordemap.domain.models.TrackItemDomainModel
import ru.newlevel.hordemap.domain.repository.LocationRepository

class DeleteSessionLocationUseCase(private val locationRepository: LocationRepository) {
    fun execute(sessionId: String, list: List<TrackItemDomainModel>?): List<TrackItemDomainModel>? {
        locationRepository.deleteLocationsBySessionId(sessionId)
        val currentList = list?.toMutableList()
        currentList?.removeAll {
            it.sessionId == sessionId
        }
        return currentList
    }
}