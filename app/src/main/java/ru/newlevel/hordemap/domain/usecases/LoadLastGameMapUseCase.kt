package ru.newlevel.hordemap.domain.usecases

import ru.newlevel.hordemap.domain.repository.GameMapRepository
import java.io.InputStream

class LoadLastGameMapUseCase(private val gameMapRepository: GameMapRepository)
{
    suspend fun execute(): InputStream? {
        return gameMapRepository.loadLastGameMap()
    }
}