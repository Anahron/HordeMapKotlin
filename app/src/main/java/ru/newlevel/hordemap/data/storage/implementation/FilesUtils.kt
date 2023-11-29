package ru.newlevel.hordemap.data.storage.implementation

import android.content.Context
import android.net.Uri
import ru.newlevel.hordemap.app.KML_EXTENSION
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class FilesUtils {
    fun replaceKMLInKMZ(oldKmzFile: Uri, newKmzFile: File, newKMLContent: String, context: Context) {
        context.contentResolver.openInputStream(oldKmzFile).use { inputStream ->
            ZipInputStream(inputStream).use { zipInputStream ->
                ZipOutputStream(FileOutputStream(newKmzFile)).use { zipOutputStream ->
                    var entry: ZipEntry?
                    while (zipInputStream.nextEntry.also {
                            entry = it } != null) {
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

    fun extractKMLContentFromKMZ(uri: Uri, context: Context): String {

            val buffer = ByteArray(10024)
            val stringBuilder = StringBuilder()
            context.contentResolver.openInputStream(uri).use { inputStream ->
                ZipInputStream(inputStream).use { zipInputStream ->
                    var entry: ZipEntry?
                    while (zipInputStream.nextEntry.also { zipEntry->
                            entry = zipEntry } != null) {
                        if (entry?.name?.endsWith(KML_EXTENSION) == true) {
                            var bytesRead: Int
                            while (zipInputStream.read(buffer).also {
                                    bytesRead = it } != -1) {
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

    fun removeDocumentTags(kmlContent: String): String {
        return kmlContent
            .replace("<Document>", "")
            .replace("</Document>", "")
    }

}