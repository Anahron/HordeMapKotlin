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

    @Query("DELETE FROM my_location_table WHERE sessionId = :sessionId")
    fun deleteLocationsBySessionId(sessionId: String)

    @Query("UPDATE my_location_table SET trackName = :newTrackName WHERE sessionId = :sessionId")
    fun renameTrackNameForSession(sessionId: String, newTrackName: String)
    @Query("UPDATE my_location_table SET favourite = :isFavourite WHERE sessionId = :sessionId")
    fun setFavouriteTrackForSession(sessionId: String, isFavourite: Boolean)

    @Update
    fun updateLocation(myLocationEntity: MyLocationEntity)

    @Insert
    fun addLocation(myLocationEntity: MyLocationEntity)

    @Insert
    fun addLocations(myLocationEntities: List<MyLocationEntity>)

    @Query("SELECT DISTINCT sessionId FROM my_location_table")
    fun getUniqueSessionIds(): List<String>
}
