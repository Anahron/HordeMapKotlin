package ru.newlevel.hordemap.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import java.util.*

@Dao
interface MyLocationDao {

    @Query("SELECT * FROM my_location_table WHERE sessionId =(:sessionId) ORDER BY date ASC")
    fun getCurrentLocationsLiveData(sessionId: String): LiveData<List<MyLocationEntity>>

    @Query("SELECT * FROM my_location_table WHERE sessionId =(:sessionId) ORDER BY date ASC")
    fun getLocationsBySessionId(sessionId: String): List<MyLocationEntity>

    @Query("DELETE FROM my_location_table WHERE sessionId = :sessionId")
    fun deleteLocationsBySessionId(sessionId: String)

    @Query("SELECT * FROM my_location_table WHERE sessionId = :sessionId ORDER BY date DESC LIMIT 2")
    fun getTwoLastLocationsInDescendingOrder(sessionId: String): List<MyLocationEntity>

    @Query("UPDATE my_location_table SET trackName = :newTrackName WHERE sessionId = :sessionId")
    fun renameTrackNameForSession(sessionId: String, newTrackName: String)
    @Query("UPDATE my_location_table SET favourite = :isFavourite WHERE sessionId = :sessionId")
    fun setFavouriteTrackForSession(sessionId: String, isFavourite: Boolean)

    @Query("UPDATE my_location_table SET sessionId = :newSessionId WHERE sessionId = :oldSessionId")
    fun updateSessionId(oldSessionId: String, newSessionId: String)
    @Update
    fun updateLocation(myLocationEntity: MyLocationEntity)

    @Query("DELETE FROM my_location_table WHERE id = :locationUuid")
    fun deleteLocationByUuid(locationUuid: String)

    @Insert
    fun addLocation(myLocationEntity: MyLocationEntity)

    @Insert
    fun addLocations(myLocationEntities: List<MyLocationEntity>)

    @Query("SELECT DISTINCT sessionId FROM my_location_table")
    fun getUniqueSessionIds(): List<String>
}
