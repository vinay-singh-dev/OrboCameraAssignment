package com.example.orbocameraassignment.image

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

            val outputStream = resolver.openOutputStream(imageUri)
                ?: run {
                    resolver.delete(imageUri, null, null)
                    return false
                }

            val compressed = outputStream.use {
                bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    90,
                    it
                )
            }

            if (!compressed) {
                resolver.delete(
                    imageUri,
                    null,
                    null
                )
                return false
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val completedValues = ContentValues().apply {
                    put(
                        MediaStore.Images.Media.IS_PENDING,
                        0
                    )
                }

                val updatedRows = resolver.update(
                    imageUri,
                    completedValues,
                    null,
                    null
                )

                if (updatedRows == 0) {
                    resolver.delete(imageUri, null, null)
                    return false
                }
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