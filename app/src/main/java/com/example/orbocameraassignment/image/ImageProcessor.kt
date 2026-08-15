package com.example.orbocameraassignment.image

import android.graphics.Bitmap
import com.example.orbocameraassignment.NativeImageProcessor

class ImageProcessor {

    private val nativeProcessor = NativeImageProcessor()

    fun crop(
        bitmap: Bitmap,
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ): Bitmap {

        return nativeProcessor.crop(
            bitmap = bitmap,
            x = x,
            y = y,
            width = width,
            height = height
        )
    }

    fun adjustImage(
        bitmap: Bitmap,
        brightness: Double,
        contrast: Double
    ): Bitmap {

        return nativeProcessor.adjustImage(
            bitmap = bitmap,
            brightness = brightness,
            contrast = contrast
        )
    }
}