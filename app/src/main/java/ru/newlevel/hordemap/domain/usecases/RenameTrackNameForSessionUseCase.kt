package ru.newlevel.hordemap.domain.usecases

import ru.newlevel.hordemap.domain.repository.LocationRepository

class RenameTrackNameForSessionUseCase(private val locationRepository: LocationRepository) {
    fun execute(sessionId: String, newTrackName:String){
        locationRepository.renameTrackNameForSession(sessionId = sessionId, newTrackName = newTrackName)
    }
}