package ru.newlevel.hordemap.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

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
    var selectedMarker: Int = 0,
    var replyOn: Long = 0,
    var isRead: Boolean = false
)