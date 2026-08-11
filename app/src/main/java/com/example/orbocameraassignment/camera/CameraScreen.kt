package com.example.orbocameraassignment.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.controls.Audio
import com.otaliastudios.cameraview.controls.Preview
import com.otaliastudios.cameraview.filter.Filters
import com.example.orbocameraassignment.image.ImageStorageManager


@Composable
fun CameraScreen() {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageStorageManager = remember {
        ImageStorageManager(context)
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

    var saveError by remember {
        mutableStateOf(false)
    }

    var cameraController by remember {
        mutableStateOf<CameraViewController?>(null)
    }

    var screenState by remember {
        mutableStateOf(CameraScreenState.CAMERA)
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
                                val saved = imageStorageManager.saveImage(bitmap)

                                saveError = !saved

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


                    Button(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp),
                        onClick = {
                            capturedBitmap = null
                            saveError = false
                            screenState = CameraScreenState.CAMERA
                        }
                    ) {
                        Text("Retake")
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