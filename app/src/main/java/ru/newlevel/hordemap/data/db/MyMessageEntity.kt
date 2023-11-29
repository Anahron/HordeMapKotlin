package ru.newlevel.hordemap.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.newlevel.hordemap.data.storage.models.MessageDataModel

@Entity(tableName = "messages")
data class MyMessageEntity(
    @PrimaryKey
    var timestamp: Long = 0,
    var userName: String = "",
    var message: String = "",
    var deviceID: String = "",
    var fileSize: Long = 0,
    var fileName: String = "",
    var url: String = "",
    var profileImageUrl: String = "",
    var selectedMarker: Int = 0
) {
    companion object {
        fun fromEntity(dataModel: MyMessageEntity): MessageDataModel {
            return  MessageDataModel().apply {
                userName = dataModel.userName
                message = dataModel.message
                timestamp = dataModel.timestamp
                deviceID = dataModel.deviceID
                fileSize = dataModel.fileSize
                fileName = dataModel.fileName
                url = dataModel.url
                profileImageUrl = dataModel.profileImageUrl
                selectedMarker = dataModel.selectedMarker
            }
        }
    }
}