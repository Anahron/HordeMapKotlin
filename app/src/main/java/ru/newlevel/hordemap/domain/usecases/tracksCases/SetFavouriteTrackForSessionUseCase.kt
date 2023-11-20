package ru.newlevel.hordemap.domain.usecases.tracksCases

import ru.newlevel.hordemap.domain.models.TrackItemDomainModel
import ru.newlevel.hordemap.domain.repository.LocationRepository

class SetFavouriteTrackForSessionUseCase(private val locationRepository: LocationRepository) {
    fun execute(sessionId: String, isFavourite: Boolean, trackItemAll: List<TrackItemDomainModel>?): List<TrackItemDomainModel>?{
        locationRepository.setFavouriteTrackForSession(sessionId, isFavourite)
        val currentList = trackItemAll?.toMutableList()
        val trackItem = currentList?.find { it.sessionId == sessionId }
        trackItem?.let {
            val updatedItem = it.copy(isFavourite = isFavourite)
            val index = currentList.indexOf(it)

            if (index != -1) {
                currentList[index] = updatedItem
            }
        }
        return currentList
    }
}