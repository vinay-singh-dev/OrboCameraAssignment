package com.example.orbocameraassignment.camera

import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Slider
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.orbocameraassignment.image.CropSelection
import com.example.orbocameraassignment.image.ImageProcessor
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.controls.Audio
import com.otaliastudios.cameraview.controls.Preview
import com.otaliastudios.cameraview.filter.Filters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.orbocameraassignment.image.ImageStorageManager


@Composable
fun CameraScreen() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageStorageManager = remember {
        ImageStorageManager(context)
    }

    val imageProcessor = remember {
        ImageProcessor()
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var capturedBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }


    var selectedRect by remember {
        mutableStateOf<androidx.compose.ui.geometry.Rect?>(null)
    }

    var croppedBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }

    var saveError by remember {
        mutableStateOf(false)
    }

    var cameraController by remember {
        mutableStateOf<CameraViewController?>(null)
    }

    var screenState by remember {
        mutableStateOf(CameraScreenState.CAMERA)
    }

    var brightness by remember {
        mutableStateOf(0f)
    }

    var contrast by remember {
        mutableStateOf(1f)
    }

    var editedBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->

                    CameraView(context).apply {

                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        audio = Audio.OFF

                        preview = Preview.GL_SURFACE

                        filter = Filters.BLACK_AND_WHITE.newInstance()

                        setLifecycleOwner(lifecycleOwner)

                        cameraController = CameraViewController(
                            cameraView = this,
                            onImageCaptured = { bitmap ->
                                capturedBitmap = bitmap
                                saveError = false
                                screenState = CameraScreenState.CAPTURED
                            }
                        )
                    }
                },
                onRelease = {
                    cameraController?.clear()
                    cameraController = null
                }
            )

            when (screenState) {

                CameraScreenState.CAMERA -> {

                    Button(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(bottom = 32.dp),
                        onClick = {
                            cameraController?.captureImage()
                        }
                    ) {
                        Text("Capture")
                    }
                }

                CameraScreenState.CAPTURED -> {

                    capturedBitmap?.let { bitmap ->

                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Captured image",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    if (saveError) {
                        Text(
                            text = "Failed to save image",
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 32.dp)
                        )
                    }


                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(bottom = 32.dp)
                    ) {

                        Button(
                            onClick = {
                                screenState = CameraScreenState.CROPPING
                            }
                        ) {
                            Text("Crop")
                        }

                        Button(

                            modifier = Modifier.padding(start = 16.dp),
                            onClick = {
                                capturedBitmap = null
                                croppedBitmap = null
                                selectedRect = null
                                saveError = false
                                screenState = CameraScreenState.CAMERA
                            }
                        ) {
                            Text("Retake")
                        }
                    }
                }

                CameraScreenState.CROPPED -> {

                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {

                        croppedBitmap?.let { bitmap ->

                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Cropped image",
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(bottom = 32.dp)
                        ) {

                            Button(
                                onClick = {
                                    screenState = CameraScreenState.EDITING
                                }
                            ) {
                                Text("Edit")
                            }

                            Button(
                                modifier = Modifier.padding(start = 16.dp),
                                onClick = {
                                    capturedBitmap = null
                                    croppedBitmap = null
                                    selectedRect = null
                                    saveError = false
                                    screenState = CameraScreenState.CAMERA
                                }
                            ) {
                                Text("Retake")
                            }
                        }
                    }
                }

                CameraScreenState.CROPPING -> {

                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {

                        capturedBitmap?.let { bitmap ->

                            CropSelection(
                                bitmap = bitmap,
                                onSelectionComplete = { rect ->
                                    selectedRect = rect
                                }
                            )
                        }

                        Button(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(bottom = 32.dp),
                            enabled = selectedRect != null,
                            onClick = {

                                val bitmap = capturedBitmap
                                val rect = selectedRect

                                if (
                                    bitmap != null &&
                                    rect != null &&
                                    rect.width > 1f &&
                                    rect.height > 1f
                                ) {

                                    croppedBitmap = imageProcessor.crop(
                                        bitmap = bitmap,
                                        x = rect.left.toInt(),
                                        y = rect.top.toInt(),
                                        width = rect.width.toInt(),
                                        height = rect.height.toInt()
                                    )

                                    screenState = CameraScreenState.CROPPED
                                }
                            }
                        ) {
                            Text("Crop")
                        }
                    }
                }

                CameraScreenState.EDITING -> {

                    val bitmap = croppedBitmap

                    if (bitmap != null) {

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .navigationBarsPadding()
                                .padding(16.dp)
                        ) {

                            Image(
                                bitmap = (editedBitmap ?: bitmap).asImageBitmap(),
                                contentDescription = "Edited image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )

                            Text("Brightness")

                            Slider(
                                value = brightness,
                                onValueChange = { value ->
                                    brightness = value

                                    scope.launch {

                                        val result = withContext(Dispatchers.Default) {
                                            imageProcessor.adjustImage(
                                                bitmap = bitmap,
                                                brightness = brightness.toDouble(),
                                                contrast = contrast.toDouble()
                                            )
                                        }

                                        editedBitmap = result
                                    }
                                },
                                valueRange = -100f..100f
                            )

                            Text("Contrast")

                            Slider(
                                value = contrast,
                                onValueChange = { value ->
                                    contrast = value

                                    scope.launch {

                                        val result = withContext(Dispatchers.Default) {
                                            imageProcessor.adjustImage(
                                                bitmap = bitmap,
                                                brightness = brightness.toDouble(),
                                                contrast = contrast.toDouble()
                                            )
                                        }

                                        editedBitmap = result
                                    }
                                },
                                valueRange = 0.5f..2f
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {

                                Button(
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        brightness = 0f
                                        contrast = 1f
                                        editedBitmap = null
                                    }
                                ) {
                                    Text("Reset")
                                }

                                Button(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 8.dp),
                                    onClick = {
                                        screenState = CameraScreenState.PREVIEW
                                    }
                                ) {
                                    Text("Done")
                                }
                            }
                        }
                    }
                }

                CameraScreenState.PREVIEW -> {

                    val bitmap = editedBitmap ?: croppedBitmap

                    if (bitmap != null) {

                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {

                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Final image",
                                modifier = Modifier.fillMaxSize()
                            )

                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .navigationBarsPadding()
                                    .padding(bottom = 32.dp)
                            ) {

                                Button(
                                    onClick = {
                                        screenState = CameraScreenState.EDITING
                                    }
                                ) {
                                    Text("Edit")
                                }

                                Button(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 16.dp),
                                    onClick = {

                                        val finalBitmap = editedBitmap ?: croppedBitmap

                                        if (finalBitmap != null) {

                                            val saved = imageStorageManager.saveImage(finalBitmap)

                                            if (saved) {
                                                screenState = CameraScreenState.CAMERA
                                                capturedBitmap = null
                                                croppedBitmap = null
                                                editedBitmap = null
                                                selectedRect = null
                                                brightness = 0f
                                                contrast = 1f
                                            } else {
                                                saveError = true
                                            }
                                        }
                                    }
                                ) {
                                    Text("Save")
                                }

                            }

                        }
                    }
                }
            }
        }


    } else {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Camera permission is required")
        }
    }
}

