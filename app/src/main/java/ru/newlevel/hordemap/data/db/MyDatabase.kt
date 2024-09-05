package ru.newlevel.hordemap.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [MyLocationEntity::class , MyMessageEntity::class, UserMarkerEntity::class, MarkerEntity::class], version = 5, exportSchema = false)
@TypeConverters(MyLocationTypeConverters::class)
abstract class MyDatabase : RoomDatabase() {
    abstract fun locationDao(): MyLocationDao
    abstract fun messageDao(): MessageDao
    abstract fun markersDao(): MarkersDao
}
