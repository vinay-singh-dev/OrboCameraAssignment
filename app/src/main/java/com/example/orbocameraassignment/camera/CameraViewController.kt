package com.example.orbocameraassignment.camera

import android.graphics.Bitmap
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult

class CameraViewController(
    private val cameraView: CameraView,
    private val onImageCaptured: (Bitmap) -> Unit
) {

    private val cameraListener = object : CameraListener() {

        override fun onPictureTaken(result: PictureResult) {
            result.toBitmap(
                1600,
                1600
            ) { bitmap ->
                bitmap?.let(onImageCaptured)
            }
        }
    }

    init {
        cameraView.addCameraListener(cameraListener)
    }

    fun captureImage() {
        if (!cameraView.isTakingPicture) {
            cameraView.takePictureSnapshot()
        }
    }

    fun clear() {
        cameraView.removeCameraListener(cameraListener)
    }
}