package ru.newlevel.hordemap.domain.usecases.mapCases

import ru.newlevel.hordemap.domain.repository.GameMapRepository

class GetAllMapsAsListUseCase(private val gameMapRepository: GameMapRepository) {
    suspend fun execute(): Result<List<Triple<String, String, Long>>> {
        val mapList = gameMapRepository.getAllMapsAsList()
        return Result.success(mapList)
    }
}