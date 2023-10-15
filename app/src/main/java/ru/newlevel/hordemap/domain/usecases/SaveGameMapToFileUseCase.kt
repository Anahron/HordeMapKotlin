package ru.newlevel.hordemap.domain.usecases

import android.net.Uri
import ru.newlevel.hordemap.domain.repository.GameMapRepository

class SaveGameMapToFileUseCase(private val gameMapRepository: GameMapRepository) {
    suspend fun execute(uri: Uri) {
        gameMapRepository.saveGameMapToFile(uri)
    }
}