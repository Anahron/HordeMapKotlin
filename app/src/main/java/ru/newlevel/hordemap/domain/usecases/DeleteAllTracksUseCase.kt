package ru.newlevel.hordemap.domain.usecases

import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.domain.repository.LocationRepository

class DeleteAllTracksUseCase(private val locationRepository: LocationRepository) {
    suspend fun execute() {
        val sessionList = locationRepository.getAllLocationsGroupedBySessionId()
        val currentSession = UserEntityProvider.sessionId.toString()
        for (session in sessionList) {
            if (session != currentSession)
                locationRepository.deleteLocationsBySessionId(sessionId = session)
        }
    }
}