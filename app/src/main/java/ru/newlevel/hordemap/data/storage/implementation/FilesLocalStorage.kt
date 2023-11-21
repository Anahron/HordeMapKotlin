package ru.newlevel.hordemap.data.storage.implementation

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.newlevel.hordemap.app.BASE_LAST_MAP_FILENAME
import ru.newlevel.hordemap.app.GPX_EXTENSION
import ru.newlevel.hordemap.app.KMZ_EXTENSION
import ru.newlevel.hordemap.data.storage.interfaces.GameMapLocalStorage
import java.io.File
import java.nio.file.Files
import kotlin.io.path.Path

class FilesLocalStorage(private val context: Context, private val filesUtils: FilesUtils) : GameMapLocalStorage {
    /*
     Из коробки кмл не умеет отображать файлы у которых в kml есть тег <Document>
     вынимаем kml, удаляем тег и перезаписываем в архив новый файл
    */
    override suspend fun saveGameMapToFile(uri: Uri, suffix: String): Uri {
        withContext(Dispatchers.IO) {
            val kmzFile = File(context.filesDir, BASE_LAST_MAP_FILENAME + KMZ_EXTENSION)
            val gpxFile = File(context.filesDir, BASE_LAST_MAP_FILENAME + GPX_EXTENSION)
            Files.deleteIfExists(Path(kmzFile.toString()))
            Files.deleteIfExists(Path(gpxFile.toString()))
            if (suffix == KMZ_EXTENSION) try {
                kmzFile.createNewFile()
                val kmlContent = filesUtils.extractKMLContentFromKMZ(uri, context)
                val modifiedKMLContent = filesUtils.removeDocumentTags(kmlContent)
                filesUtils.replaceKMLInKMZ(uri, kmzFile, modifiedKMLContent, context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            else try {
                context.contentResolver.openInputStream(uri).use { inputStream ->
                    context.openFileOutput(gpxFile.toString(), Context.MODE_PRIVATE).use {
                        inputStream?.copyTo(it)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return File(context.filesDir, BASE_LAST_MAP_FILENAME + suffix).toUri()
    }

    override suspend fun loadLastMapFromFile(): Uri? {
        var filename = BASE_LAST_MAP_FILENAME + KMZ_EXTENSION
        var file = File(context.filesDir, filename)
        if (!file.exists()) {
            filename = BASE_LAST_MAP_FILENAME + GPX_EXTENSION
            file = File(context.filesDir, filename)
        }
        return Uri.fromFile(file)
    }
}