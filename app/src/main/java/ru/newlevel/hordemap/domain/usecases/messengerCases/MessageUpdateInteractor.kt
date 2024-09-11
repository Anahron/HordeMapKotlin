package ru.newlevel.hordemap.domain.usecases.messengerCases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import ru.newlevel.hordemap.app.mapToDomainModel
import ru.newlevel.hordemap.data.db.MyMessageEntity
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.repository.MessengerRepository

class MessageUpdateInteractor(private val messengerRepository: MessengerRepository) {

    private var localData = messengerRepository.getLocalMessageUpdate()

    suspend fun syncMessagesData() {
        val remoteData = messengerRepository.getRemoteMessagesUpdate()
        try {
           remoteData.combine(localData) { remoteMessages, localMessages ->
                compareMessage(remoteMessages, localMessages)
            }.collect {}
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun compareMessage(remoteMessages: List<MyMessageEntity>, localMessages: List<MyMessageEntity>) {
        for (remoteMessage in remoteMessages) {
            val localMessage = localMessages.find { it.timestamp == remoteMessage.timestamp }
            if (localMessage != null) {
                if (remoteMessage.userName != localMessage.userName || remoteMessage.selectedMarker != localMessage.selectedMarker || remoteMessage.profileImageUrl != localMessage.profileImageUrl || remoteMessage.message != localMessage.message) {
                    val newMessage = localMessage.copy(
                        userName = remoteMessage.userName,
                        message = remoteMessage.message,
                        selectedMarker = remoteMessage.selectedMarker,
                        profileImageUrl = remoteMessage.profileImageUrl,
                    )
                    messengerRepository.updateLocalMessage(newMessage) // Вызов suspend-функции напрямую
                }
            } else {
                messengerRepository.insertLocalMessage(remoteMessage) // Вызов insert напрямую
            }
        }
        for (localMessage in localMessages) {
            if (remoteMessages.none { it.timestamp == localMessage.timestamp }) {
                messengerRepository.deleteLocalMessage(localMessage) // Вызов delete напрямую
            }
        }
    }

    fun getNewMessageCountUpdate(): Flow<Int> = messengerRepository.getNewMessageCount()
    fun getMessageUpdate(): Flow<List<MyMessageEntity>> = localData

    fun getUsersProfiles(): Flow<List<UserDomainModel>> = messengerRepository.getUsersProfilesUpdate().map {
        it.map { s ->
            s.mapToDomainModel()
        }
    }
}
