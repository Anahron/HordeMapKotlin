package ru.newlevel.hordemap.data.storage.interfaces

import android.net.Uri

interface ProfilePhotoStorage {
    suspend fun uploadProfilePhoto(uri: Uri, fileName: String): Result<Uri>
}