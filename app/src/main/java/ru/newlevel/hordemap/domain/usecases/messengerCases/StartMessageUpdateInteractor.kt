package ru.newlevel.hordemap.domain.usecases.messengerCases

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.newlevel.hordemap.app.mapToDomainModel
import ru.newlevel.hordemap.data.db.MyMessageEntity
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.repository.MessengerRepository

class StartMessageUpdateInteractor(private val messengerRepository: MessengerRepository) {

    fun getMessageUpdate(): LiveData<List<MyMessageEntity>> = messengerRepository.getLocalMessageUpdate()


    fun getDownloadProgress(): MutableLiveData<Int> = messengerRepository.getDownloadProgress()

    suspend fun startMessageUpdate() = withContext(Dispatchers.IO) {
            messengerRepository.startMessageUpdate()
    }

    suspend fun getUsersProfiles(): MutableLiveData<List<UserDomainModel>> {
        return withContext(Dispatchers.IO) {
            messengerRepository.getUsersProfiles().map {
                it.map { s ->
                    s.mapToDomainModel()
                }
            } as MutableLiveData<List<UserDomainModel>>
        }
    }
}