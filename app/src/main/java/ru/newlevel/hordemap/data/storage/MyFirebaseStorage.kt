package ru.newlevel.hordemap.data.storage

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.*
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

    override suspend fun sendFile(message: String, uri: Uri, fileName: String?, fileSize: Long): String {
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
            request.allowScanningByMediaScanner()
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
        CoroutineScope(Dispatchers.IO).launch {
            var downloading = true
            while (downloading) {
                val cursor =
                    downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
                if (cursor.moveToFirst()) {
                    when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_SUCCESSFUL, DownloadManager.STATUS_FAILED -> {
                            downloading = false
                            progressLiveData.postValue(1000)
                        }
                        else -> {
                            val bytesDownloaded =
                                cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                            val bytesTotal =
                                cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                            val percent = bytesDownloaded * 100.0f / bytesTotal
                            progressLiveData.postValue(percent.toInt())
                        }
                    }
                }
                cursor.close()
                delay(500) // Ожидание 0.5 секунды перед следующей проверкой
            }
        }
    }
}


