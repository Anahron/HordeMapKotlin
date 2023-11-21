package ru.newlevel.hordemap.domain.usecases.mapCases

import android.net.Uri
import ru.newlevel.hordemap.domain.repository.GameMapRepository

class SaveGameMapToFileUseCase(private val gameMapRepository: GameMapRepository) {
    suspend fun execute(uri: Uri, suffix: String) {
        gameMapRepository.saveGameMapToFile(uri, suffix)
    }
}