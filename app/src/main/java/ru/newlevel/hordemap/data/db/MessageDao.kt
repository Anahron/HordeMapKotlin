package ru.newlevel.hordemap.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MyMessageEntity)

    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessagesLiveData(): LiveData<List<MyMessageEntity>>

    @Delete
    suspend fun deleteMessage(message: MyMessageEntity)
    @Update
    suspend fun updateMessage(message: MyMessageEntity)

}