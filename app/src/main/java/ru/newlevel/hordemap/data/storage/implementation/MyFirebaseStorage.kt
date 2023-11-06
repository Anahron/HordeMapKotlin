package ru.newlevel.hordemap.data.storage.implementation

import android.app.DownloadManager
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import ru.newlevel.hordemap.data.storage.interfaces.GameMapRemoteStorage
import ru.newlevel.hordemap.data.storage.interfaces.MessageFilesStorage
import java.io.File
import kotlin.coroutines.resume

private const val MESSAGE_FILE_FOLDER = "MessengerFiles0"
private const val MAP_URL = "gs://horde-4112c.appspot.com/maps/map.kmz"  // карта полигона

class MyFirebaseStorage : GameMapRemoteStorage, MessageFilesStorage {

    private val storageReference = FirebaseStorage.getInstance().reference
    private val gsReference = storageReference.storage.getReferenceFromUrl(MAP_URL)

    private val progressLiveData = MutableLiveData<Int>()

    override suspend fun loadGameMapFromServer(context: Context): Uri? {
        return suspendCancellableCoroutine { continuation ->
            val filename = "lastSavedMap.kmz"
            val file = File(context.filesDir, filename)
            gsReference.getFile(file)
                .addOnSuccessListener { _ ->
                    continuation.resume(Uri.fromFile(file))
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }
            continuation.invokeOnCancellation {
                continuation.resume(null)
            }
        }
    }

    override suspend fun sendFile(
        message: String,
        uri: Uri,
        fileName: String?,
        fileSize: Long
    ): String {
        return withContext(Dispatchers.IO) {
            val messageFilesStorage = storageReference.child("$MESSAGE_FILE_FOLDER/$fileName")
            val uploadTask = messageFilesStorage.putFile(uri)
            val resultDeferred = CompletableDeferred<String?>()

            uploadTask.addOnProgressListener { taskSnapshot: UploadTask.TaskSnapshot ->
                val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                progressLiveData.postValue(progress.toInt())
            }
            uploadTask.addOnCompleteListener { task: Task<UploadTask.TaskSnapshot?> ->
                if (task.isSuccessful) {
                    progressLiveData.postValue(1000)
                    messageFilesStorage.downloadUrl
                        .addOnSuccessListener { uri: Uri ->
                            val downloadUrl = uri.toString()
                            resultDeferred.complete(downloadUrl)
                        }
                        .addOnFailureListener {
                            resultDeferred.complete("")
                        }
                } else {
                    resultDeferred.complete("")
                }
            }
            resultDeferred.await().toString()
        }
    }

    override fun getDownloadProgress(): MutableLiveData<Int> {
        return progressLiveData
    }

    override fun downloadFile(context: Context, uri: Uri, fileName: String?) {
        try {
            val request = DownloadManager.Request(uri)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setAllowedOverMetered(true)
            request.setAllowedOverRoaming(true)
            MediaScannerConnection.scanFile(context, arrayOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + fileName),null,null)
            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
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
                                    progressLiveData.value = 1000
                                }
                                else -> {
                                    val columnIndexBytesDownloaded =
                                        it.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                                    val columnIndexBytesTotal =
                                        it.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                                    if (columnIndexBytesDownloaded != -1 && columnIndexBytesTotal != -1) {
                                        val bytesDownloaded = it.getInt(columnIndexBytesDownloaded)
                                        val bytesTotal = cursor.getInt(columnIndexBytesTotal)
                                        val percent = bytesDownloaded * 100.0f / bytesTotal
                                        progressLiveData.value = percent.toInt()
                                    }
                                }
                            }
                        }
                    }
                }
                delay(500) // Ожидание 0.5 секунды перед следующей проверкой
                progressLiveData.value = 1000
            }
        }
    }

}


