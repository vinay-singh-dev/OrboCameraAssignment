package com.example.orbocameraassignment.image

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import kotlin.math.abs
import kotlin.math.min

@Composable
fun CropSelection(
    bitmap: Bitmap,
    onSelectionComplete: (Rect) -> Unit
) {

    var startPoint by remember {
        mutableStateOf<Offset?>(null)
    }

    var currentPoint by remember {
        mutableStateOf<Offset?>(null)
    }

    var containerWidth by remember {
        mutableStateOf(0)
    }

    var containerHeight by remember {
        mutableStateOf(0)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                containerWidth = size.width
                containerHeight = size.height
            }
    )
    {

        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Image to crop",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {

                    detectDragGestures(

                        onDragStart = { offset ->
                            startPoint = offset
                            currentPoint = offset
                        },

                        onDrag = { change, _ ->
                            currentPoint = change.position
                        },

                        onDragEnd = {

                            val start = startPoint
                            val end = currentPoint

                            if (
                                start != null &&
                                end != null &&
                                containerWidth > 0 &&
                                containerHeight > 0
                            ) {

                                val scale = min(
                                    containerWidth.toFloat() / bitmap.width,
                                    containerHeight.toFloat() / bitmap.height
                                )

                                val displayedWidth = bitmap.width * scale
                                val displayedHeight = bitmap.height * scale

                                val offsetX = (containerWidth - displayedWidth) / 2f
                                val offsetY = (containerHeight - displayedHeight) / 2f

                                val screenLeft = min(start.x, end.x)
                                val screenTop = min(start.y, end.y)
                                val screenRight = maxOf(start.x, end.x)
                                val screenBottom = maxOf(start.y, end.y)

                                val bitmapLeft =
                                    ((screenLeft - offsetX) / scale)
                                        .coerceIn(0f, bitmap.width.toFloat())

                                val bitmapTop =
                                    ((screenTop - offsetY) / scale)
                                        .coerceIn(0f, bitmap.height.toFloat())

                                val bitmapRight =
                                    ((screenRight - offsetX) / scale)
                                        .coerceIn(0f, bitmap.width.toFloat())

                                val bitmapBottom =
                                    ((screenBottom - offsetY) / scale)
                                        .coerceIn(0f, bitmap.height.toFloat())

                                onSelectionComplete(
                                    Rect(
                                        left = bitmapLeft,
                                        top = bitmapTop,
                                        right = bitmapRight,
                                        bottom = bitmapBottom
                                    )
                                )
                            }
                        }
                    )
                }
        ) {

            val start = startPoint
            val end = currentPoint

            if (start != null && end != null) {

                drawRect(
                    color = Color.White,
                    topLeft = Offset(
                        min(start.x, end.x),
                        min(start.y, end.y)
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        abs(end.x - start.x),
                        abs(end.y - start.y)
                    ),
                    style = Stroke(width = 4f)
                )
            }
        }
    }
}