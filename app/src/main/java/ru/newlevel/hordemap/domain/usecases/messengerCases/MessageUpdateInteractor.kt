package ru.newlevel.hordemap.domain.usecases.messengerCases

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.newlevel.hordemap.app.TAG
import ru.newlevel.hordemap.app.mapToDomainModel
import ru.newlevel.hordemap.data.db.MyMessageEntity
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.repository.MessengerRepository

class MessageUpdateInteractor(private val messengerRepository: MessengerRepository) {

    private var localData = messengerRepository.getLocalMessageUpdate()

    suspend fun syncMessagesData() {
        Log.e(TAG, " syncMessagesData() in MessageUpdateInteractor ")
        val remoteData = messengerRepository.getRemoteMessagesUpdate()
        try {
           remoteData.combine(localData) { remoteMessages, localMessages ->
                if (remoteMessages.size > localMessages.size) {
                    val commonMessages = remoteMessages.intersect(localMessages.toSet())
                    val lastCommonMessage = commonMessages.maxByOrNull { it.timestamp }
                    val remoteMessagesAfterLastCommon = remoteMessages.dropWhile { it != lastCommonMessage }
                    val messagesCountDifference = remoteMessagesAfterLastCommon.size - 1
                    messengerRepository.incrementNewMessageCount(messagesCountDifference)
                }
                compareMessage(remoteMessages, localMessages)
            }.collect {}
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun compareMessage(remoteMessages: List<MyMessageEntity>, localMessages: List<MyMessageEntity>) {
        for (remoteMessage in remoteMessages) {
            val localMessage = localMessages.find { it.timestamp == remoteMessage.timestamp }
            if (localMessage != null) {
                if (remoteMessage.userName != localMessage.userName || remoteMessage.selectedMarker != localMessage.selectedMarker || remoteMessage.profileImageUrl != localMessage.profileImageUrl || remoteMessage.message != localMessage.message) {
                    val newMessage = localMessage.copy(
                        userName = remoteMessage.userName,
                        message = remoteMessage.message,
                        selectedMarker = remoteMessage.selectedMarker,
                        profileImageUrl = remoteMessage.profileImageUrl
                    )
                    CoroutineScope(Dispatchers.IO).launch {
                        messengerRepository.updateLocalMessage(newMessage)
                    }
                }
            } else CoroutineScope(Dispatchers.IO).launch {
                messengerRepository.insertLocalMessage(remoteMessage)
            }
        }
        for (localMessage in localMessages) {
            if (remoteMessages.none { it.timestamp == localMessage.timestamp }) {
                CoroutineScope(Dispatchers.IO).launch {
                    messengerRepository.deleteLocalMessage(localMessage)
                }
            }
        }
    }

    fun resetNewMessageCount() {
        messengerRepository.incrementNewMessageCount(-1)
    }

    fun getNewMessageCountUpdate(): Flow<Int> = messengerRepository.getNewMessageCount()
    fun getMessageUpdate(): Flow<List<MyMessageEntity>> = localData

    fun getUsersProfiles(): Flow<List<UserDomainModel>> = messengerRepository.getUsersProfilesUpdate().map {
        it.map { s ->
            s.mapToDomainModel()
        }
    }
}
