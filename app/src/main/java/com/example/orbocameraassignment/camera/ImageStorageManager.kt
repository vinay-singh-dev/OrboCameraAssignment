package com.example.orbocameraassignment.camera

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore

class ImageStorageManager(
    private val context: Context
) {

    fun saveImage(bitmap: Bitmap): Boolean {

        val filename = "Orbo_${System.currentTimeMillis()}.jpg"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "Pictures/OrboCamera"
                )
                put(
                    MediaStore.Images.Media.IS_PENDING,
                    1
                )
            }
        }

        val resolver = context.contentResolver

        val imageUri = resolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: return false

        return try {

            resolver.openOutputStream(imageUri)?.use { outputStream ->

                bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    90,
                    outputStream
                )
            } ?: false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(
                    MediaStore.Images.Media.IS_PENDING,
                    0
                )

                resolver.update(
                    imageUri,
                    contentValues,
                    null,
                    null
                )
            }

            true

        } catch (exception: Exception) {

            resolver.delete(
                imageUri,
                null,
                null
            )

            false
        }
    }
}