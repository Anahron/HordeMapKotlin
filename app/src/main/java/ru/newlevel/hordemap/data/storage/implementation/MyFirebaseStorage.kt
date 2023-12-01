package ru.newlevel.hordemap.data.storage.implementation

import android.app.DownloadManager
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import ru.newlevel.hordemap.app.BASE_LAST_MAP_FILENAME
import ru.newlevel.hordemap.app.KMZ_EXTENSION
import ru.newlevel.hordemap.app.MAP_URL
import ru.newlevel.hordemap.app.MESSAGE_FILE_FOLDER
import ru.newlevel.hordemap.app.PROFILE_PHOTO_FOLDER
import ru.newlevel.hordemap.data.storage.interfaces.GameMapRemoteStorage
import ru.newlevel.hordemap.data.storage.interfaces.MessageFilesStorage
import ru.newlevel.hordemap.data.storage.interfaces.ProfilePhotoStorage
import java.io.File
import kotlin.coroutines.resume

class MyFirebaseStorage : GameMapRemoteStorage, MessageFilesStorage, ProfilePhotoStorage {

    private val storageReference = FirebaseStorage.getInstance().reference
    private val gsReference = storageReference.storage.getReferenceFromUrl(MAP_URL)

    override suspend fun downloadGameMapFromServer(context: Context): Uri? {
        return suspendCancellableCoroutine { continuation ->
            val filename = BASE_LAST_MAP_FILENAME + KMZ_EXTENSION
            val file = File(context.filesDir, filename)
            gsReference.getFile(file).addOnSuccessListener { _ ->
                continuation.resume(Uri.fromFile(file))
            }.addOnFailureListener {
                continuation.resume(null)
            }
            continuation.invokeOnCancellation {
                continuation.resume(null)
            }
        }
    }

    override suspend fun uploadProfilePhoto(uri: Uri, fileName: String): Result<Uri> {
        val messageFilesStorage = storageReference.child("$PROFILE_PHOTO_FOLDER/$fileName")
        return uploadTask(messageFilesStorage, uri)
    }

    override suspend fun uploadFile(uri: Uri, fileName: String?): Result<Uri> {
        val messageFilesStorage = storageReference.child("$MESSAGE_FILE_FOLDER/$fileName")
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

