package ru.newlevel.hordemap.domain.usecases

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream


class LoadGameMapFromUriUseCase {
    suspend fun execute(uri: Uri, context: Context): InputStream? {
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)
            } catch (e: Exception) {
                null
            }
        }
    }
}