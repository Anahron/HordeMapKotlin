package ru.newlevel.hordemap.data.storage

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream

class FilesLocalStorage(private val context: Context): GameMapLocalStorage {

    override suspend fun saveGameMapToFile(uri: Uri) {
        val filename = "lastSavedMap.kmz"
        try {
            val inputStream = context.contentResolver.openInputStream(uri)

            if (inputStream != null) {
                val outputStream: OutputStream = context.openFileOutput(filename, Context.MODE_PRIVATE)
                inputStream.copyTo(outputStream)
                withContext(Dispatchers.IO) {
                    inputStream.close()
                }
                withContext(Dispatchers.IO) {
                    outputStream.close()
                }
            } else {
                Log.e("AAA", "Не удалось открыть inputStream")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun loadLastMapFromFile(): InputStream? {
        try {
            val filename = "lastSavedMap.kmz"
            val file = File(context.filesDir, filename)
            if (file.exists()) {
                return withContext(Dispatchers.IO) {
                    FileInputStream(file)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}