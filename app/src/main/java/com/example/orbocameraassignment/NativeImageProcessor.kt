package com.example.orbocameraassignment

class NativeImageProcessor {

    companion object {

        init {
            System.loadLibrary("native_image_processor")
        }
    }

    external fun stringFromJNI(): String
}