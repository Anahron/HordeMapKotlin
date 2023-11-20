package ru.newlevel.hordemap.domain.usecases.mapCases

import android.net.Uri
import ru.newlevel.hordemap.domain.repository.GameMapRepository

class LoadLastGameMapUseCase(private val gameMapRepository: GameMapRepository)
{
    suspend fun execute(): Uri? {
        return gameMapRepository.loadLastGameMap()
    }
}