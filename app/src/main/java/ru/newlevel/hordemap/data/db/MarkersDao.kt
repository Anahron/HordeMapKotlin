package ru.newlevel.hordemap.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MarkersDao {
    //UserMarkerEntity
    @Transaction
    suspend fun refreshUserMarkers(userMarkers: List<MarkerEntity>) {
        deleteAllUserMarkers()
        insertAllUserMarkers(userMarkers)
    }

    @Query("DELETE FROM staticMarker WHERE local = 0")
    suspend fun deleteAllUserMarkers()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllUserMarkers(userMarkers: List<MarkerEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserMarker(userMarkerEntity: MarkerEntity)

    @Query("SELECT * FROM usersMarker WHERE deviceId = :deviceId")
    fun getUserMarker(deviceId: String): Flow<List<MarkerEntity>>


    //StaticMarkerEntity
    @Transaction
    suspend fun refreshStaticMarkers(staticMarkers: List<MarkerEntity>) {
        deleteAllStaticMarkers()
        insertAllMarkers(staticMarkers)
    }
    @Query("DELETE FROM staticMarker WHERE local = 0")
    suspend fun deleteAllStaticMarkers()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMarkers(markers: List<MarkerEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarker(marker: MarkerEntity)

    @Query("SELECT * FROM staticMarker WHERE deviceId = :deviceId")
    fun getMarker(deviceId: String): Flow<List<MarkerEntity>>

}