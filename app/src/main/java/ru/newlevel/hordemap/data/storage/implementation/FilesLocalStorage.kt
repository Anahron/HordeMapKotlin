package ru.newlevel.hordemap.data.storage.implementation

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.newlevel.hordemap.app.BASE_LAST_MAP_FILENAME
import ru.newlevel.hordemap.app.GPX_EXTENSION
import ru.newlevel.hordemap.app.KML_EXTENSION
import ru.newlevel.hordemap.app.KMZ_EXTENSION
import ru.newlevel.hordemap.app.getInputSteamFromUri
import ru.newlevel.hordemap.data.storage.interfaces.GameMapLocalStorage
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.io.path.Path

class FilesLocalStorage(private val context: Context) : GameMapLocalStorage {
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
                val kmlContent = extractKMLContentFromKMZ(uri)
                val modifiedKMLContent = removeDocumentTags(kmlContent)
                replaceKMLInKMZ(uri, kmzFile, modifiedKMLContent)
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

    private fun replaceKMLInKMZ(oldKmzFile: Uri, newKmzFile: File, newKMLContent: String) {
        context.contentResolver.openInputStream(oldKmzFile).use {
            ZipInputStream(it).use { zipInputStream ->
                ZipOutputStream(FileOutputStream(newKmzFile)).use { zipOutputStream ->
                    var entry: ZipEntry?
                    while (zipInputStream.nextEntry.also { entry = it } != null) {
                        if (entry?.name?.endsWith(KML_EXTENSION) == true) {
                            zipOutputStream.putNextEntry(ZipEntry(entry?.name))
                            zipOutputStream.write(newKMLContent.toByteArray())
                            zipOutputStream.closeEntry()
                        } else {
                            zipOutputStream.putNextEntry(ZipEntry(entry?.name))
                            val buffer = ByteArray(1024)
                            var bytesRead: Int
                            while (zipInputStream.read(buffer).also { bytesRead = it } != -1) {
                                zipOutputStream.write(buffer, 0, bytesRead)
                            }
                            zipOutputStream.closeEntry()
                        }
                        zipInputStream.closeEntry()
                    }
                }
            }
        }
    }

    private suspend fun extractKMLContentFromKMZ(uri: Uri): String {
        val buffer = ByteArray(10024)
        val stringBuilder = StringBuilder()
        getInputSteamFromUri(uri, context).use {
            ZipInputStream(it).use { zipInputStream ->
                var entry: ZipEntry?
                while (zipInputStream.nextEntry.also { entry = it } != null) {
                    if (entry?.name?.endsWith(KML_EXTENSION) == true) {
                        var bytesRead: Int
                        while (zipInputStream.read(buffer).also { bytesRead = it } != -1) {
                            stringBuilder.append(buffer.decodeToString(0, bytesRead))
                        }
                        zipInputStream.closeEntry()
                        return stringBuilder.toString()
                    }
                }
            }
        }
        return ""
    }

    private fun removeDocumentTags(kmlContent: String): String {
        return kmlContent
            .replace("<Document>", "")
            .replace("</Document>", "")
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