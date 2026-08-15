package com.example.orbocameraassignment.image

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Rect
import androidx.core.graphics.createBitmap

class ImageProcessor {

    fun crop(
        bitmap: Bitmap,
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ): Bitmap {

        val sourceMat = Mat()

        Utils.bitmapToMat(bitmap, sourceMat)

        val cropRect = Rect(
            x,
            y,
            width,
            height
        )

        val croppedMat = sourceMat.submat(cropRect)

        val croppedBitmap = createBitmap(width, height)

        Utils.matToBitmap(
            croppedMat,
            croppedBitmap
        )

        sourceMat.release()
        croppedMat.release()

        return croppedBitmap
    }

    fun adjustBrightness(
        bitmap: Bitmap,
        brightness: Double
    ): Bitmap {

        val sourceMat = Mat()

        Utils.bitmapToMat(bitmap, sourceMat)

        sourceMat.convertTo(
            sourceMat,
            -1,
            1.0,
            brightness
        )

        val resultBitmap = createBitmap(
            bitmap.width,
            bitmap.height
        )

        Utils.matToBitmap(
            sourceMat,
            resultBitmap
        )

        sourceMat.release()

        return resultBitmap
    }

    fun adjustContrast(
        bitmap: Bitmap,
        contrast: Double
    ): Bitmap {

        val sourceMat = Mat()

        Utils.bitmapToMat(bitmap, sourceMat)

        sourceMat.convertTo(
            sourceMat,
            -1,
            contrast,
            0.0
        )

        val resultBitmap = createBitmap(
            bitmap.width,
            bitmap.height
        )

        Utils.matToBitmap(
            sourceMat,
            resultBitmap
        )

        sourceMat.release()

        return resultBitmap
    }

    fun adjustImage(
        bitmap: Bitmap,
        brightness: Double,
        contrast: Double
    ): Bitmap {

        val sourceMat = Mat()

        Utils.bitmapToMat(bitmap, sourceMat)

        sourceMat.convertTo(
            sourceMat,
            -1,
            contrast,
            brightness
        )

        val resultBitmap = createBitmap(
            bitmap.width,
            bitmap.height
        )

        Utils.matToBitmap(
            sourceMat,
            resultBitmap
        )

        sourceMat.release()

        return resultBitmap
    }

}