package ru.newlevel.hordemap.domain.usecases

import ru.newlevel.hordemap.domain.repository.LocationRepository

class SaveCurrentTrackUseCase(private val locationRepository: LocationRepository) {
    fun execute(sessionId: String){
        try {
            locationRepository.saveCurrentTrackSession(sessionId, (sessionId.toLong() + System.currentTimeMillis()).toString())
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
}