package ru.newlevel.hordemap.data.storage

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.*
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.data.storage.models.MessageDataModel
import java.io.File
import kotlin.coroutines.resume

private const val GEO_USER_MARKERS_PATH = "geoData0"
private const val GEO_STATIC_MARKERS_PATH = "geoMarkers0"
private const val MESSAGE_FILE_FOLDER = "MessengerFiles0"
private const val TIME_TO_DELETE_USER_MARKER = 30 // в минутах
private const val MAP_URL = "gs://horde-4112c.appspot.com/maps/map.kmz"  // карта полигона
private const val MESSAGE_PATH = "messages0"
private const val TIMESTAMP_PATH = "timestamp"
private const val TAG = "AAA"

class StorageImpl : MarkersDataStorage, MapStorage, MessageStorage {

    private val databaseReference = FirebaseDatabase.getInstance().reference
    private val storageReference = FirebaseStorage.getInstance().reference

    private val gsReference =
        this.storageReference.storage.getReferenceFromUrl(MAP_URL)

    private val staticDatabaseReference = databaseReference.child(GEO_STATIC_MARKERS_PATH)
    private val userDatabaseReference = databaseReference.child(GEO_USER_MARKERS_PATH)

    private var valueUserEventListener: ValueEventListener? = null
    private var valueStaticEventListener: ValueEventListener? = null
    private var valueMessageEventListener: ValueEventListener? = null

    private val liveDataStaticMarkers = MutableLiveData<List<MarkerDataModel>>()
    private val liveDataUserMarkers = MutableLiveData<List<MarkerDataModel>>()
    private val liveDataMessageDataModel = MutableLiveData<List<MessageDataModel>>()
    private val progressLiveData = MutableLiveData<Int>()

    override fun deleteStaticMarker(marker: Marker) {
        staticDatabaseReference.child(marker.tag.toString()).removeValue()
    }

    override fun sendUserMarker(markerModel: MarkerDataModel) {
        userDatabaseReference.child(markerModel.deviceId).setValue(markerModel)
    }

    override fun startMessageUpdate(): MutableLiveData<List<MessageDataModel>> {
        if (valueMessageEventListener != null)
            databaseReference.removeEventListener(valueMessageEventListener!!)
        valueMessageEventListener = databaseReference.child(MESSAGE_PATH).orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val messages = ArrayList<MessageDataModel>()
                    for (snapshot in dataSnapshot.children) {
                        val message: MessageDataModel? =
                            snapshot.getValue(MessageDataModel::class.java)
                        if (message != null) {
                            messages.add(message)
                        }
                    }
                    if (messages.isNotEmpty()) {
                        liveDataMessageDataModel.postValue(messages)
                    }
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
        return liveDataMessageDataModel
    }

    override fun startUserMarkerUpdates(): MutableLiveData<List<MarkerDataModel>> {
        if (valueUserEventListener != null)
            userDatabaseReference.removeEventListener(valueUserEventListener!!)
        valueUserEventListener =
            userDatabaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val savedUserMarkers: ArrayList<MarkerDataModel> = ArrayList()
                    val timeNow = System.currentTimeMillis()
                    for (snapshot in dataSnapshot.children) {
                        try {
                            var alpha: Float
                            val timestamp: Long? =
                                snapshot.child(TIMESTAMP_PATH).getValue(Long::class.java)
                            val timeDiffMillis = timeNow - timestamp!!
                            val timeDiffMinutes = timeDiffMillis / 60000
                            // Удаляем маркера в базе, которым больше TIME_TO_DELETE_USER_MARKER минут
                            alpha = if (timeDiffMinutes >= TIME_TO_DELETE_USER_MARKER) {
                                snapshot.ref.removeValue()
                                continue
                            } else {
                                // Устанавливаем прозрачность маркера от 0 до 5 минут максимум до 50%
                                1f - (timeDiffMinutes / 10f).coerceAtMost(0.5f)
                            }
                            val myMarker: MarkerDataModel =
                                snapshot.getValue(MarkerDataModel::class.java)!!
                            myMarker.deviceId = snapshot.key.toString()
                            myMarker.alpha = alpha
                            savedUserMarkers.add(myMarker)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    liveDataUserMarkers.postValue(savedUserMarkers)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        return liveDataUserMarkers
    }

    override fun startStaticMarkerUpdates(): MutableLiveData<List<MarkerDataModel>> {
        if (valueStaticEventListener != null)
            staticDatabaseReference.removeEventListener(valueStaticEventListener!!)
        valueStaticEventListener =
            staticDatabaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val savedStaticMarkers: ArrayList<MarkerDataModel> = ArrayList()
                    for (snapshot in dataSnapshot.children) {
                        try {
                            val myMarker: MarkerDataModel =
                                snapshot.getValue(MarkerDataModel::class.java)!!
                            savedStaticMarkers.add(myMarker)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    liveDataStaticMarkers.postValue(savedStaticMarkers)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        return liveDataStaticMarkers
    }

    override fun sendStaticMarker(markerModel: MarkerDataModel) {
        staticDatabaseReference.child(markerModel.timestamp.toString()).setValue(markerModel)
    }

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

    override fun sendFile(message: String, uri: Uri, fileName: String?, fileSize: Long) {
        val messageFilesStorage = this.storageReference.child("$MESSAGE_FILE_FOLDER/$fileName")
        val uploadTask = messageFilesStorage.putFile(uri)
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
                        val fileNameToSend = fileName ?: ""
                        sendMessage(message, downloadUrl, fileSize, fileNameToSend)
                    }
            }
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
        } catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun sendMessage(text: String) {
        val time = System.currentTimeMillis()
        val geoDataPath = "$MESSAGE_PATH/$time"
        val updates: MutableMap<String, Any> = HashMap()
        updates["$geoDataPath/userName"] = UserEntityProvider.userEntity?.name.toString()
        updates["$geoDataPath/message"] = text
        updates["$geoDataPath/deviceID"] = UserEntityProvider.userEntity?.deviceID.toString()
        updates["$geoDataPath/timestamp"] = time
        databaseReference.updateChildren(updates)
    }

    private fun sendMessage(text: String, downloadUrl: String, fileSize: Long, fileName: String) {
        val time = System.currentTimeMillis()
        val geoDataPath = "$MESSAGE_PATH/$time"
        val updates: MutableMap<String, Any> = HashMap()
        updates["$geoDataPath/userName"] = UserEntityProvider.userEntity?.name.toString()
        updates["$geoDataPath/message"] = text
        updates["$geoDataPath/url"] = downloadUrl
        updates["$geoDataPath/deviceID"] = UserEntityProvider.userEntity?.deviceID.toString()
        updates["$geoDataPath/timestamp"] = time
        updates["$geoDataPath/fileSize"] = fileSize
        updates["$geoDataPath/fileName"] = fileName
        databaseReference.updateChildren(updates)
    }

    override fun stopMarkerUpdates() {
        Log.e(TAG, "stopMarkerUpdates in StorageImpl вызван")
        if (valueUserEventListener != null)
            userDatabaseReference.removeEventListener(valueUserEventListener!!)
        if (valueStaticEventListener != null)
            staticDatabaseReference.removeEventListener(valueStaticEventListener!!)
    }

    override fun stopMessageUpdate() {
        Log.e(TAG, "stopMessageUpdate in StorageImpl вызван")
        if (valueMessageEventListener != null)
            databaseReference.removeEventListener(valueMessageEventListener!!)
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


