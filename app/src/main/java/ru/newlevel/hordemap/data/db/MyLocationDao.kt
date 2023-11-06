package ru.newlevel.hordemap.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import java.util.*

@Dao
interface MyLocationDao {

    @Query("SELECT * FROM my_location_table WHERE sessionId =(:sessionId) ORDER BY date DESC")
    fun getLocationsSortedByUpdateTime(sessionId: String): List<MyLocationEntity>

    @Update
    fun updateLocation(myLocationEntity: MyLocationEntity)

    @Insert
    fun addLocation(myLocationEntity: MyLocationEntity)

    @Insert
    fun addLocations(myLocationEntities: List<MyLocationEntity>)
}
