package ru.newlevel.hordemap.domain.usecases.mapCases

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.newlevel.hordemap.app.ImageCompressor
import ru.newlevel.hordemap.app.getFileNameFromUri
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.domain.repository.SettingsRepository

class LoadProfilePhotoUseCase(private val settingsRepository: SettingsRepository) {
    suspend fun execute(uri: Uri, context: Context): Result<Uri> {
        val imageCompressor = ImageCompressor()
        val compressedImage = withContext(Dispatchers.IO) {
            imageCompressor.compressImageAndSaveToFile(context, uri, 300, 300, 60)
        }
        var filename = context.getFileNameFromUri(uri)
        if (filename.isEmpty())
            filename = "JPEG_" + UserEntityProvider.userEntity.authName + System.currentTimeMillis() + ".jpg"
        return compressedImage?.let { settingsRepository.uploadProfilePhoto(uri = it, fileName = filename) }
            ?: settingsRepository.uploadProfilePhoto(uri = uri, fileName = filename)
    }
}