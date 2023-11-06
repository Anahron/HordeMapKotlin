package ru.newlevel.hordemap.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [MyLocationEntity::class], version = 1, exportSchema = false)
@TypeConverters(MyLocationTypeConverters::class)
abstract class MyLocationDatabase : RoomDatabase() {
    abstract fun locationDao(): MyLocationDao
}
