package ru.newlevel.hordemap.domain.usecases.mapCases

import android.content.Context
import android.net.Uri
import android.util.Log
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.GPX_EXTENSION
import ru.newlevel.hordemap.app.KMZ_EXTENSION
import ru.newlevel.hordemap.app.getFileNameFromUri
import ru.newlevel.hordemap.domain.repository.GameMapRepository

class SaveGameMapToFileUseCase(private val gameMapRepository: GameMapRepository) {
    suspend fun execute(uri: Uri, context: Context): Result<Uri?> {
        val mimeType = context.getFileNameFromUri(uri)
        Log.e("AAA", "SaveGameMapToFileUseCase mimeType = " + mimeType.toString())
        return when {
            mimeType?.endsWith(KMZ_EXTENSION) == true -> {
                gameMapRepository.saveGameMapToFile(uri, KMZ_EXTENSION)
            }
            mimeType?.endsWith(GPX_EXTENSION) == true -> {
                gameMapRepository.saveGameMapToFile(uri, GPX_EXTENSION)
            }
            else ->
                Result.failure(Throwable(context.getString(R.string.wrong_format_file)))
        }
    }
}
