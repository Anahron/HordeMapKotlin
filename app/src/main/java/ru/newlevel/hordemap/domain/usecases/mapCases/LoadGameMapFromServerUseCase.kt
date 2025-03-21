package ru.newlevel.hordemap.domain.usecases.mapCases

import KmzFileProcessor
import android.content.Context
import android.net.Uri
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.domain.repository.GameMapRepository

class LoadGameMapFromServerUseCase(private val gameMapRepository: GameMapRepository) {
    suspend fun execute(context: Context, url: String): Result<Uri> {
        return runCatching {
            gameMapRepository.loadGameMapFromServer(context, url)?.let { uri ->
                KmzFileProcessor(context).processKmzFromUri(uri)
            } ?: return Result.failure(Throwable(context.getString(R.string.load_map_filed)))
        }
    }
}

//class LoadGameMapFromServerUseCase(private val gameMapRepository: GameMapRepository) {
//    suspend fun execute(context: Context, url: String): Result<Uri> {
//        val uri = gameMapRepository.loadGameMapFromServer(context, url)
//        return if (uri != null)
//            Result.success(uri)
//        else
//            Result.failure(Throwable(context.getString(R.string.load_map_filed)))
//    }
//}
