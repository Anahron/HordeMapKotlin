package ru.newlevel.hordemap.domain.usecases

import ru.newlevel.hordemap.domain.repository.LocationRepository

class SetFavouriteTrackForSessionUseCase(private val locationRepository: LocationRepository) {
    fun execute(sessionId: String, isFavourite: Boolean){
        locationRepository.setFavouriteTrackForSession(sessionId, isFavourite)
    }
}