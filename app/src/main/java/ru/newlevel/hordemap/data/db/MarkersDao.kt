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
    suspend fun refreshUserMarkers(userMarkers: List<UserMarkerEntity>) {
        deleteAllUserMarkers()
        insertAllUserMarkers(userMarkers)
    }

    @Query("DELETE FROM usersMarker WHERE local = 0")
    suspend fun deleteAllUserMarkers()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllUserMarkers(userMarkers: List<UserMarkerEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserMarker(userMarkerEntity: UserMarkerEntity)

    @Query("SELECT * FROM usersMarker")
    fun getUserMarker(): Flow<List<UserMarkerEntity>>


    //StaticMarkerEntity
    @Transaction
    suspend fun refreshStaticMarkers(staticMarkers: List<MarkerEntity>) {
        deleteAllStaticMarkers()
        insertAllMarkers(staticMarkers)
    }
    @Query("DELETE FROM staticMarker WHERE local = 0")
    suspend fun deleteAllStaticMarkers()

    @Query("DELETE FROM staticMarker WHERE timestamp = :id")
    suspend fun deleteMarker(id: Long)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMarkers(markers: List<MarkerEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarker(marker: MarkerEntity)
    @Query("SELECT * FROM staticMarker WHERE timestamp = :id")
    suspend fun getSingleMarker(id: Long): MarkerEntity

    @Query("SELECT * FROM staticMarker")
    fun getMarker(): Flow<List<MarkerEntity>>

}