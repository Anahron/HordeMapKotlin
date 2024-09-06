package ru.newlevel.hordemap.data.storage.implementation

import android.app.DownloadManager
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import ru.newlevel.hordemap.app.BASE_LAST_MAP_FILENAME
import ru.newlevel.hordemap.app.KMZ_EXTENSION
import ru.newlevel.hordemap.app.MAPS_FOLDER_URL
import ru.newlevel.hordemap.app.MESSAGE_FILE_FOLDER
import ru.newlevel.hordemap.app.PROFILE_PHOTO_FOLDER
import ru.newlevel.hordemap.app.TAG
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.data.storage.interfaces.GameMapRemoteStorage
import ru.newlevel.hordemap.data.storage.interfaces.MessageFilesStorage
import ru.newlevel.hordemap.data.storage.interfaces.ProfilePhotoStorage
import java.io.File
import kotlin.coroutines.resume

class MyFirebaseStorage : GameMapRemoteStorage, MessageFilesStorage, ProfilePhotoStorage {

    private val storageReference = FirebaseStorage.getInstance().reference

    override suspend fun downloadGameMapFromServer(context: Context, url: String): Uri? {
        return suspendCancellableCoroutine { continuation ->
            val filename = BASE_LAST_MAP_FILENAME + KMZ_EXTENSION
            val file = File(context.filesDir, filename)
            storageReference.storage.getReferenceFromUrl(url).getFile(file).addOnSuccessListener { _ ->
                continuation.resume(Uri.fromFile(file))
            }.addOnFailureListener {
                continuation.resume(null)
            }
            continuation.invokeOnCancellation {
                continuation.resume(null)
            }
        }
    }


    override suspend fun getAllMapsAsList(): List<Triple<String, String, Long>> {
        return suspendCancellableCoroutine { continuation ->
            // Получаем ссылку на папку с картами
            val directoryRef = storageReference.storage.getReferenceFromUrl(MAPS_FOLDER_URL)
            // Получаем список всех файлов в папке
            directoryRef.listAll()
                .addOnSuccessListener { listResult ->
                    Log.e(TAG, ".addOnSuccessListener  listResult -> ${listResult.items}")
                    val fileList = mutableListOf<Triple<String, String, Long>>()

                    // Для каждого файла получаем его URL и имя
                    val tasks = listResult.items.map { itemRef ->
                        itemRef.metadata.addOnSuccessListener { metadata ->
                            val fileName = itemRef.name // Имя файла
                            val fileSize = metadata.sizeBytes // Размер файла
                            fileList.add(Triple(fileName, itemRef.toString(), fileSize))
                        }
                    }

                    // Дожидаемся завершения всех задач по получению URL
                    Tasks.whenAllComplete(tasks).addOnSuccessListener {
                        continuation.resume(fileList)
                    }.addOnFailureListener {
                        continuation.resume(emptyList())
                    }
                }
                .addOnFailureListener {
                    continuation.resume(emptyList())
                }

            continuation.invokeOnCancellation {
                continuation.resume(emptyList())
            }
        }
    }


    override suspend fun uploadProfilePhoto(uri: Uri, fileName: String): Result<Uri> {
        val messageFilesStorage = storageReference.child("$PROFILE_PHOTO_FOLDER${UserEntityProvider.userEntity.userGroup}/$fileName")
        return uploadTask(messageFilesStorage, uri)
    }

    override suspend fun uploadFile(uri: Uri, fileName: String?): Result<Uri> {
        val messageFilesStorage = storageReference.child("$MESSAGE_FILE_FOLDER${UserEntityProvider.userEntity.userGroup}/$fileName")
        return uploadTask(messageFilesStorage, uri)
    }

    private suspend fun uploadTask(storage: StorageReference, uri: Uri): Result<Uri> {
        return try {
            withContext(Dispatchers.IO) {
                val uploadTask = storage.putFile(uri).await()
                if (uploadTask.task.isSuccessful) {
                    try {
                        val downloadUrl = storage.downloadUrl.await()
                        Result.success(downloadUrl)
                    } catch (downloadException: Exception) {
                        Result.failure(downloadException)
                    }
                } else {
                    Result.failure(Throwable("Failed to upload file"))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun downloadFile(context: Context, uri: Uri, fileName: String?): Result<Boolean> {
        try {
            val request = DownloadManager.Request(uri)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setAllowedOverMetered(true)
            request.setAllowedOverRoaming(true)
            MediaScannerConnection.scanFile(
                context, arrayOf(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        .toString() + "/" + fileName
                ), null, null
            )
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(e)
        }
        return Result.success(true)
    }

}

