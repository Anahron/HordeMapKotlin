package ru.newlevel.hordemap.data.storage.implementation

import android.app.DownloadManager
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import ru.newlevel.hordemap.app.BASE_LAST_MAP_FILENAME
import ru.newlevel.hordemap.app.KMZ_EXTENSION
import ru.newlevel.hordemap.app.MAP_URL
import ru.newlevel.hordemap.app.MESSAGE_FILE_FOLDER
import ru.newlevel.hordemap.app.PROFILE_PHOTO_FOLDER
import ru.newlevel.hordemap.app.TAG
import ru.newlevel.hordemap.data.storage.interfaces.GameMapRemoteStorage
import ru.newlevel.hordemap.data.storage.interfaces.MessageFilesStorage
import ru.newlevel.hordemap.data.storage.interfaces.ProfilePhotoStorage
import java.io.File
import kotlin.coroutines.resume

class MyFirebaseStorage : GameMapRemoteStorage, MessageFilesStorage, ProfilePhotoStorage {

    private val storageReference = FirebaseStorage.getInstance().reference
    private val gsReference = storageReference.storage.getReferenceFromUrl(MAP_URL)
    private val progressLiveData = MutableLiveData<Int>()

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

    private suspend fun uploadTask(storage: StorageReference, uri: Uri): Result<Uri>{
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


    override fun getDownloadProgress(): MutableLiveData<Int> {
        return progressLiveData
    }

    override fun downloadFile(context: Context, uri: Uri, fileName: String?) {
        Log.e(TAG,  "downloadFile(context: Context, uri: Uri, fileName: String?) = " + this)
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
            val downloadId = downloadManager.enqueue(request)
            observeDownloadProgress(downloadManager, downloadId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun observeDownloadProgress(downloadManager: DownloadManager, downloadId: Long) {
        CoroutineScope(Dispatchers.Main).launch {
            var downloading = true
            while (downloading) {
                val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
                cursor.use {
                    if (it.moveToFirst()) {
                        val columnIndexStatus = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        if (columnIndexStatus != -1) {
                            when (it.getInt(columnIndexStatus)) {
                                DownloadManager.STATUS_SUCCESSFUL, DownloadManager.STATUS_FAILED -> {
                                    downloading = false
                                    Log.e(TAG,  " DownloadManager.STATUS_SUCCESSFUL, DownloadManager.STATUS_FAILED " + this)
                                    progressLiveData.postValue( 1000)
                                }

                                DownloadManager.STATUS_RUNNING  -> {
                                    val columnIndexBytesDownloaded =
                                        it.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                                    val columnIndexBytesTotal = it.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                                    if (columnIndexBytesDownloaded != -1 && columnIndexBytesTotal != -1) {
                                        val bytesDownloaded = it.getInt(columnIndexBytesDownloaded)
                                        val bytesTotal = cursor.getInt(columnIndexBytesTotal)
                                        val percent = bytesDownloaded * 100.0f / bytesTotal
                                        Log.e(TAG,  " Download percent " + percent)
                                        progressLiveData.postValue(percent.toInt())
                                    }
                                }
                            }
                        }
                    }
                }
                cursor.close()
                delay(500) // Ожидание 0.5 секунды перед следующей проверкой
            }
        }
    }
}


