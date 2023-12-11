package ru.newlevel.hordemap.domain.usecases.messengerCases

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.newlevel.hordemap.app.mapToDomainModel
import ru.newlevel.hordemap.data.db.MyMessageEntity
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.repository.MessengerRepository

class StartMessageUpdateInteractor(private val messengerRepository: MessengerRepository) {

    fun getMessageUpdate(): LiveData<List<MyMessageEntity>> = messengerRepository.getLocalMessageUpdate()

    suspend fun startMessageUpdate() = withContext(Dispatchers.IO) {
        messengerRepository.startMessageUpdate()
    }

   fun getUsersProfiles(): Flow<List<UserDomainModel>> = messengerRepository.getUsersProfiles().map {
        it.map { s ->
            s.mapToDomainModel()
        }
    }
}