package ru.newlevel.hordemap.data.storage.interfaces

import kotlinx.coroutines.flow.Flow

interface MessagesCountLocalStorage {
    fun getNewMessageCount(): Flow<Int>
    fun incrementNewMessageCount(increment: Int)
}