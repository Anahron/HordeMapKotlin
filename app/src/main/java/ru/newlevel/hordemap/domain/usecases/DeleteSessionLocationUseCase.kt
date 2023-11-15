package ru.newlevel.hordemap.domain.usecases

import ru.newlevel.hordemap.domain.repository.LocationRepository

class DeleteSessionLocationUseCase(private val locationRepository: LocationRepository) {
    fun execute(sessionId: String){
        locationRepository.deleteLocationsBySessionId(sessionId)
    }
}