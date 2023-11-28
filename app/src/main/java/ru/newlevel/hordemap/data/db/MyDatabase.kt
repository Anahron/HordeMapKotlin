package ru.newlevel.hordemap.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [MyLocationEntity::class , MyMessageEntity::class], version = 1, exportSchema = false)
@TypeConverters(MyLocationTypeConverters::class)
abstract class MyDatabase : RoomDatabase() {
    abstract fun locationDao(): MyLocationDao
    abstract fun messageDao(): MessageDao
}
