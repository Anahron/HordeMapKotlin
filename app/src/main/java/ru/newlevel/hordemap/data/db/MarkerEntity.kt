package ru.newlevel.hordemap.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "staticMarker")
data class MarkerEntity(
    @PrimaryKey
    var deviceId: String = "",
    var userName: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var timestamp: Long = 0,
    var item: Int = 0,
    var title: String = "",
    var alpha: Float = 1f,
    var local: Boolean = false
)

@Entity(tableName = "usersMarker")
data class UserMarkerEntity(
    @PrimaryKey
    var deviceId: String = "",
    var userName: String = "",
    val latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var timestamp: Long = 0,
    var item: Int = 0,
    var title: String = "",
    var alpha: Float = 1f,
    var local: Boolean = false
)