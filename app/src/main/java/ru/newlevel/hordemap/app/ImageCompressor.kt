package ru.newlevel.hordemap.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.roundToInt

class ImageCompressor {

    fun compressImageAndSaveToFile(
        context: Context, imageUri: Uri, maxWidth: Int, maxHeight: Int, quality: Int
    ): Uri? {
        try {
            context.contentResolver.openInputStream(imageUri).use { inputStream ->
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeStream(inputStream, null, options)

                val imageHeight = options.outHeight
                val imageWidth = options.outWidth
                var scaleFactor = 1

                if (imageWidth > maxWidth || imageHeight > maxHeight) {
                    val widthRatio = (imageWidth.toFloat() / maxWidth.toFloat()).roundToInt()
                    val heightRatio = (imageHeight.toFloat() / maxHeight.toFloat()).roundToInt()
                    scaleFactor = if (widthRatio < heightRatio) widthRatio else heightRatio
                }
                options.inSampleSize = scaleFactor
                options.inJustDecodeBounds = false
                val bitmap: Bitmap?
                context.contentResolver.openInputStream(imageUri).use {
                    bitmap = BitmapFactory.decodeStream(it, null, options)
                }
                val file = createTempImageFile(context)
                if (file != null) {
                    FileOutputStream(file).use { fileOutputStream ->
                        bitmap?.compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream)
                        fileOutputStream.flush()
                        bitmap?.recycle()
                    }
                }
                return Uri.fromFile(file)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun createTempImageFile(context: Context): File? {
        return try {
            val timeStamp: String = System.currentTimeMillis().toString()
            val imageFileName = "JPEG_" + timeStamp + "_"
            val storageDir: File = context.filesDir
            File.createTempFile(imageFileName, ".jpg", storageDir)
        } catch (e: IOException) {
            null
        }
    }
}