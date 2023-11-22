package ru.newlevel.hordemap.domain.usecases.tracksCases

import ru.newlevel.hordemap.domain.models.TrackItemDomainModel
import ru.newlevel.hordemap.domain.repository.LocationRepository

class RenameTrackNameForSessionUseCase(private val locationRepository: LocationRepository) {
    fun execute(sessionId: String, newTrackName:String, list: List<TrackItemDomainModel>?):List<TrackItemDomainModel>?{
        locationRepository.renameTrackNameForSession(sessionId = sessionId, newTrackName = newTrackName)
        val currentList = list?.toMutableList()
        val trackItem = currentList?.find { it.sessionId == sessionId }
        trackItem?.let {
            val updatedItem = it.copy(title = newTrackName)
            val index = currentList.indexOf(it)

            if (index != -1) {
                currentList[index] = updatedItem
            }
        }
        return currentList
    }
}