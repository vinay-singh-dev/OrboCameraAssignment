package com.example.orbocameraassignment

import android.graphics.Bitmap

class NativeImageProcessor {

    companion object {

        init {
            System.loadLibrary("native_image_processor")
        }
    }



    external fun crop(
        bitmap: Bitmap,
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ): Bitmap

    external fun adjustImage(
        bitmap: Bitmap,
        brightness: Double,
        contrast: Double
    ): Bitmap
}
