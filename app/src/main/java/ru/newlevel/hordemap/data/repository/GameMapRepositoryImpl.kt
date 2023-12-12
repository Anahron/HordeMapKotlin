package ru.newlevel.hordemap.data.repository

import android.content.Context
import android.net.Uri
import ru.newlevel.hordemap.data.storage.interfaces.GameMapRemoteStorage
import ru.newlevel.hordemap.data.storage.interfaces.GameMapLocalStorage
import ru.newlevel.hordemap.domain.repository.GameMapRepository

class GameMapRepositoryImpl(
    private val gameMapLocalStorage: GameMapLocalStorage,
    private val gameMapRemoteStorage: GameMapRemoteStorage
) : GameMapRepository {

    override suspend fun loadGameMapFromServer(context: Context): Uri? =
        gameMapRemoteStorage.downloadGameMapFromServer(context)

    override suspend fun saveGameMapToFile(uri: Uri, suffix: String): Result<Uri?> =
        gameMapLocalStorage.saveGameMapToFile(uri, suffix)
}