package ru.newlevel.hordemap.data.repository

import android.content.Context
import android.net.Uri
import ru.newlevel.hordemap.data.storage.MapStorage
import ru.newlevel.hordemap.data.storage.GameMapLocalStorage
import ru.newlevel.hordemap.domain.repository.GameMapRepository

class GameMapRepositoryImpl(private val gameMapLocalStorage: GameMapLocalStorage, private val mapStorage: MapStorage): GameMapRepository {

    override suspend fun loadGameMapFromServer(context: Context): Uri? {
       return mapStorage.loadGameMapFromServer(context)
    }

    override suspend fun loadLastGameMap(): Uri? {
       return gameMapLocalStorage.loadLastMapFromFile()
    }

    override suspend fun saveGameMapToFile(uri: Uri) {
       gameMapLocalStorage.saveGameMapToFile(uri)
    }
}