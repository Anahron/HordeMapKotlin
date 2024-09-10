package ru.newlevel.hordemap.domain.usecases.mapCases

import ru.newlevel.hordemap.data.storage.models.MapFileModel
import ru.newlevel.hordemap.domain.repository.GameMapRepository

class GetAllMapsAsListUseCase(private val gameMapRepository: GameMapRepository) {
    suspend fun execute(): Result<List<MapFileModel>> {
        val mapList = gameMapRepository.getAllMapsAsList().sortedBy { it ->
            it.name
        }
        return Result.success(mapList)
    }
}