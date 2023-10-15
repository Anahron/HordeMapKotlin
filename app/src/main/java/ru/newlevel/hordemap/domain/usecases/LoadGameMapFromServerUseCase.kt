package ru.newlevel.hordemap.domain.usecases

import android.content.Context
import android.net.Uri
import ru.newlevel.hordemap.domain.repository.GameMapRepository

class LoadGameMapFromServerUseCase(private val gameMapRepository: GameMapRepository) {
    suspend fun execute(context: Context): Uri? {
      return gameMapRepository.loadGameMapFromServer(context)
    }
}