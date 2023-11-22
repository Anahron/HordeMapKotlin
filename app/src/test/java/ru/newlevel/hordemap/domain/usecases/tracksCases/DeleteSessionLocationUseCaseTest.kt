package ru.newlevel.hordemap.domain.usecases.tracksCases

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import ru.newlevel.hordemap.domain.models.TrackItemDomainModel
import ru.newlevel.hordemap.domain.repository.LocationRepository

class DeleteSessionLocationUseCaseTest {

    @Test
    fun `execute deletes session locations and updates the list`() = runBlocking {
        val locationRepository = mock<LocationRepository>()

        val sessionIdToDelete = "session2"
        val trackList = listOf(
            TrackItemDomainModel(sessionId = "session1", date = "12.24.24", locations = ArrayList()),
            TrackItemDomainModel(sessionId = "session2", date = "12.24.24", locations = ArrayList()),
            TrackItemDomainModel(sessionId = "session3", date = "12.24.24", locations = ArrayList()),
        )

        val deleteSessionLocationUseCase = DeleteSessionLocationUseCase(locationRepository)

        val updatedTrackList = deleteSessionLocationUseCase.execute(sessionIdToDelete, trackList)

        verify(locationRepository, times(1)).deleteLocationsBySessionId(sessionIdToDelete)

        assertNotNull(updatedTrackList)
        assertEquals(2, updatedTrackList?.size)
    }
}