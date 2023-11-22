package ru.newlevel.hordemap.domain.usecases.tracksCases

import org.junit.Assert.*

import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import ru.newlevel.hordemap.domain.models.TrackItemDomainModel
import ru.newlevel.hordemap.domain.repository.LocationRepository

class RenameTrackNameForSessionUseCaseTest {

    @Test
    fun `execute renames track name and updates list`() {

        val locationRepository = mock<LocationRepository>()

        val useCase = RenameTrackNameForSessionUseCase(locationRepository)

        val sessionId = "1"
        val newTrackName = "New Track Name"

        val fakeList = listOf(TrackItemDomainModel(sessionId = "1", date = "s", locations = ArrayList()), TrackItemDomainModel(sessionId = "2", date = "2", locations = ArrayList()))

        val updatedList = useCase.execute(sessionId, newTrackName, fakeList)

        verify(locationRepository).renameTrackNameForSession(sessionId, newTrackName)
        assertEquals(fakeList.size, updatedList?.size)
    }
}
