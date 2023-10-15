package ru.newlevel.hordemap.data.repository

import android.content.Context
import android.net.Uri
import ru.newlevel.hordemap.data.storage.FirebaseMapStorage
import ru.newlevel.hordemap.data.storage.GameMapLocalStorage
import ru.newlevel.hordemap.domain.repository.GameMapRepository
import java.io.InputStream

class GameMapRepositoryImpl(private val gameMapLocalStorage: GameMapLocalStorage, private val firebaseMapStorage: FirebaseMapStorage): GameMapRepository {

    override suspend fun loadGameMapFromServer(context: Context): Uri? {
       return firebaseMapStorage.loadGameMapFromServer(context)
    }

    override suspend fun loadLastGameMap(): InputStream? {
       return gameMapLocalStorage.loadLastMapFromFile()
    }

    override suspend fun saveGameMapToFile(uri: Uri) {
       gameMapLocalStorage.saveGameMapToFile(uri)
    }
}