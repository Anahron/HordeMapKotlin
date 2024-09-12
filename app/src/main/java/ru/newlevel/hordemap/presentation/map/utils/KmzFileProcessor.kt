package ru.newlevel.hordemap.presentation.map.utils

import android.content.Context
import android.net.Uri
import ru.newlevel.hordemap.app.BASE_LAST_MAP_FILENAME
import ru.newlevel.hordemap.app.KMZ_EXTENSION
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/*
    В некоторых KML присутствует тег <Document> который не понимает KMLLayer
    Распаковываем kmz, удаляем тег, запаковываем обратно
    */

class KmzFileProcessor(private val context: Context) {
    private fun unzipKmzFromStream(inputStream: InputStream, outputDir: File) {
        ZipInputStream(BufferedInputStream(inputStream)).use { zipInputStream ->
            generateSequence { zipInputStream.nextEntry }.forEach { zipEntry ->
                val file = File(outputDir, zipEntry.name)
                if (zipEntry.isDirectory) {
                    file.mkdirs()
                } else {
                    file.outputStream().use { fileOutputStream ->
                        zipInputStream.copyTo(fileOutputStream)
                    }
                }
                zipInputStream.closeEntry()
            }
        }
    }

    private fun removeDocumentTags(kmlFile: File) {
        val content = kmlFile.readText()
        val updatedContent = content.replace(Regex("(?s)\\s*<Document>"), "")
            .replace(Regex("(?s)\\s*</Document>"), "")
        kmlFile.writeText(updatedContent)
    }

    private fun zipKmz(sourceDir: File, kmzFile: File) {
        ZipOutputStream(FileOutputStream(kmzFile)).use { zipOut ->
            sourceDir.walkTopDown().filter { it.isFile }.forEach { file ->
                val zipEntry = ZipEntry(file.relativeTo(sourceDir).path)
                zipOut.putNextEntry(zipEntry)
                file.inputStream().use { it.copyTo(zipOut) }
                zipOut.closeEntry()
            }
        }
    }

    fun processKmzFromUri(kmzUri: Uri): Uri {
        val outputDir = File(context.cacheDir, "kmz_temp").apply { mkdir() }
        context.contentResolver.openInputStream(kmzUri)?.use { inputStream -> unzipKmzFromStream(inputStream, outputDir) }

        outputDir.walkTopDown().find { it.extension == "kml" }?.let { kmlFile ->
            removeDocumentTags(kmlFile)
        }

        val newKmzFile = File(context.filesDir, BASE_LAST_MAP_FILENAME + KMZ_EXTENSION)
        zipKmz(outputDir, newKmzFile)

        outputDir.deleteRecursively()

        return Uri.fromFile(newKmzFile)
    }
}