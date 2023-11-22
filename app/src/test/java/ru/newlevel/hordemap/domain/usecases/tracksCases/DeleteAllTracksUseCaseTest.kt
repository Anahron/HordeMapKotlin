package ru.newlevel.hordemap.domain.usecases.tracksCases

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.Mockito.*
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.domain.repository.LocationRepository

class DeleteAllTracksUseCaseTest {

    @Test
    fun `execute deletes all tracks except the current session`() = runBlocking {
        val locationRepository = mock<LocationRepository>()
        val deleteAllTracksUseCase = DeleteAllTracksUseCase(locationRepository)

        val sessions = listOf("1", "2", "3")
        `when`(locationRepository.getAllLocationsGroupedBySessionId()).thenReturn(sessions)

        UserEntityProvider.sessionId = 1

        deleteAllTracksUseCase.execute()

        verify(locationRepository).deleteLocationsBySessionId("2")
        verify(locationRepository).deleteLocationsBySessionId("3")
        verify(locationRepository,  never()).deleteLocationsBySessionId("1")
    }
}