package ru.newlevel.hordemap.domain.usecases.messengerCases

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.newlevel.hordemap.app.mapToDataModel
import ru.newlevel.hordemap.app.mapToDomainModel
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.data.storage.models.MessageDataModel
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.repository.MessengerRepository

class StartMessageUpdateInteractor(private val messengerRepository: MessengerRepository) {
    suspend fun getMessageUpdate(): MutableLiveData<List<MessageDataModel>> {
        return withContext(Dispatchers.IO){ messengerRepository.getMessageUpdate()}
    }

    fun getDownloadProgress(): MutableLiveData<Int> {
        return messengerRepository.getDownloadProgress()
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

    suspend fun sendUserData(): Boolean{
       return withContext(Dispatchers.IO) {
            messengerRepository.sendUserData(
                UserEntityProvider.userEntity.mapToDataModel()
            )
        }
    }
}