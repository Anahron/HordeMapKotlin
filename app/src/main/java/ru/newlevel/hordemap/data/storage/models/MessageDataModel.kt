package ru.newlevel.hordemap.data.storage.models

import android.graphics.Bitmap
import java.io.File
import java.sql.Timestamp

class MessageDataModel {
    var userName: String = ""
    var message: String = ""
    var timestamp: Long = 0
    var deviceID: String = ""
    var file: File? = null
    var thumbnail: Bitmap? = null
}
