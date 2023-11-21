package ru.newlevel.hordemap.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.DateFormat
import java.util.Date
import java.util.UUID

@Entity(tableName = "my_location_table")
data class MyLocationEntity(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val sessionId: String,
    val trackName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val date: Date = Date(),
    val favourite: Boolean = false
) {

    override fun toString(): String {
        return "$latitude, $longitude on " + "${DateFormat.getDateTimeInstance().format(date)}.\n"
    }
}
