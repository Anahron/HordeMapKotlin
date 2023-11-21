package ru.newlevel.hordemap.data.storage.implementation

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.newlevel.hordemap.data.storage.interfaces.GameMapLocalStorage
import java.io.File
import java.io.OutputStream

class FilesLocalStorage(private val context: Context) : GameMapLocalStorage {

    override suspend fun saveGameMapToFile(uri: Uri, suffix: String) {
        val filename = "lastSavedMap$suffix"
        File(context.filesDir, "lastSavedMap.kmz").delete()
        File(context.filesDir, "lastSavedMap.gpx").delete()
        try {
            val inputStream = context.contentResolver.openInputStream(uri)

            if (inputStream != null) {
                val outputStream: OutputStream =
                    context.openFileOutput(filename, Context.MODE_PRIVATE)
                inputStream.copyTo(outputStream)
                withContext(Dispatchers.IO) {
                    inputStream.close()
                }
                withContext(Dispatchers.IO) {
                    outputStream.close()
                }
            } else {
                Log.e("AAA", "Open inputStream failed")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun loadLastMapFromFile(): Uri? {
        var filename = "lastSavedMap.kmz"
        var file = File(context.filesDir, filename)
        if (!file.exists()) {
            filename = "lastSavedMap.gpx"
            file = File(context.filesDir, filename)
        }
        return Uri.fromFile(file)
    }
}