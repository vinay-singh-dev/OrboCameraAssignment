package com.example.orbocameraassignment.camera

import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Slider
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.FilterBAndW
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.Color
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.FilterBAndW
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.media3.effect.Crop
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.orbocameraassignment.image.ImageStorageManager
import kotlinx.coroutines.Job


@Composable
fun CameraScreen() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val Accent = Color(0xFF7C8CFF)
    val imageStorageManager = remember {
        ImageStorageManager(context)
    }

    val imageProcessor = remember {
        ImageProcessor()
    }

    var processingJob by remember {
        mutableStateOf<Job?>(null)
    }

    var isBlackAndWhite by remember {
        mutableStateOf(false)
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

                        filter = Filters.NONE.newInstance()

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

                update = { cameraView ->

                    cameraView.filter = if (isBlackAndWhite) {
                        Filters.BLACK_AND_WHITE.newInstance()
                    } else {
                        Filters.NONE.newInstance()
                    }
                },

                onRelease = {
                    cameraController?.clear()
                    cameraController = null
                }
            )

            when (screenState) {

                CameraScreenState.CAMERA -> {

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(
                                start = 32.dp,
                                end = 32.dp,
                                bottom = 28.dp
                            )
                            .fillMaxWidth(),

                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // B&W FILTER
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            IconButton(
                                onClick = {
                                    isBlackAndWhite = !isBlackAndWhite
                                },

                                modifier = Modifier
                                    .size(58.dp)
                                    .background(
                                        color = if (isBlackAndWhite) {
                                            Color.White
                                        } else {
                                            Color.Black.copy(alpha = 0.55f)
                                        },
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = Color.White.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                            ) {

                                Icon(
                                    imageVector = Icons.Default.FilterBAndW,
                                    contentDescription = "Black and white filter",
                                    tint = if (isBlackAndWhite) {
                                        Color.Black
                                    } else {
                                        Color.White
                                    }
                                )
                            }

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(
                                text = "B&W",
                                color = Color.White
                            )
                        }

                        // CAMERA SHUTTER
                        IconButton(
                            onClick = {
                                cameraController?.captureImage()
                            },

                            modifier = Modifier.size(84.dp)
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .background(
                                        Color.White,
                                        CircleShape
                                    )
                                    .border(
                                        width = 4.dp,
                                        color = Color.White.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    ),

                                contentAlignment = Alignment.Center
                            ) {

                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(
                                            Color.White,
                                            CircleShape
                                        )
                                        .border(
                                            width = 2.dp,
                                            color = Color.Black.copy(alpha = 0.15f),
                                            shape = CircleShape
                                        )
                                )
                            }
                        }

                        // Keeps shutter centered
                        Spacer(
                            modifier = Modifier.size(58.dp)
                        )
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
                            .padding(
                                horizontal = 32.dp,
                                vertical = 24.dp
                            )
                            .fillMaxWidth(),

                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // Crop
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            IconButton(
                                onClick = {
                                    screenState = CameraScreenState.CROPPING
                                },

                                modifier = Modifier
                                    .size(58.dp)
                                    .background(
                                        color = Color.Black.copy(alpha = 0.60f),
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = Color.White.copy(alpha = 0.35f),
                                        shape = CircleShape
                                    )
                            ) {

                                Icon(
                                    imageVector = Icons.Default.Crop,
                                    contentDescription = "Crop image",
                                    tint = Color.White
                                )
                            }

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(
                                text = "Crop",
                                color = Color.White
                            )
                        }

                        // Retake
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            IconButton(
                                onClick = {

                                    capturedBitmap = null
                                    croppedBitmap = null
                                    selectedRect = null
                                    saveError = false

                                    screenState = CameraScreenState.CAMERA
                                },

                                modifier = Modifier
                                    .size(58.dp)
                                    .background(
                                        color = Color.Black.copy(alpha = 0.60f),
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = Color.White.copy(alpha = 0.35f),
                                        shape = CircleShape
                                    )
                            ) {

                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Retake photo",
                                    tint = Color.White
                                )
                            }

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(
                                text = "Retake",
                                color = Color.White
                            )
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
                                .padding(
                                    horizontal = 32.dp,
                                    vertical = 24.dp
                                )
                                .fillMaxWidth(),

                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            // EDIT
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                IconButton(
                                    onClick = {
                                        screenState = CameraScreenState.EDITING
                                    },

                                    modifier = Modifier
                                        .size(58.dp)
                                        .background(
                                            color = Color.Black.copy(alpha = 0.60f),
                                            shape = CircleShape
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = Color.White.copy(alpha = 0.35f),
                                            shape = CircleShape
                                        )
                                ) {

                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Edit image",
                                        tint = Color.White
                                    )
                                }

                                Spacer(
                                    modifier = Modifier.height(6.dp)
                                )

                                Text(
                                    text = "Edit",
                                    color = Color.White
                                )
                            }

                            // RETAKE
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                IconButton(
                                    onClick = {

                                        capturedBitmap = null
                                        croppedBitmap = null
                                        selectedRect = null
                                        editedBitmap = null

                                        brightness = 0f
                                        contrast = 1f

                                        saveError = false

                                        screenState = CameraScreenState.CAMERA
                                    },

                                    modifier = Modifier
                                        .size(58.dp)
                                        .background(
                                            color = Color.Black.copy(alpha = 0.60f),
                                            shape = CircleShape
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = Color.White.copy(alpha = 0.35f),
                                            shape = CircleShape
                                        )
                                ) {

                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Retake photo",
                                        tint = Color.White
                                    )
                                }

                                Spacer(
                                    modifier = Modifier.height(6.dp)
                                )

                                Text(
                                    text = "Retake",
                                    color = Color.White
                                )
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

                        IconButton(
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
                            },

                            enabled = selectedRect != null,

                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(bottom = 28.dp)
                                .size(68.dp)
                                .background(
                                    color = if (selectedRect != null) {
                                        Accent
                                    } else {
                                        Color.Gray.copy(alpha = 0.55f)
                                    },
                                    shape = CircleShape
                                )
                        ) {

                            Icon(
                                imageVector = Icons.Default.Crop,
                                contentDescription = "Crop image",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }

                CameraScreenState.EDITING -> {

                    val bitmap = croppedBitmap

                    if (bitmap != null) {

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF101114))
                        ) {

                            // ------------------------------------------------------------
                            // IMAGE
                            // ------------------------------------------------------------

                            Image(
                                bitmap = (editedBitmap ?: bitmap).asImageBitmap(),

                                contentDescription = "Edited image",

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        top = 60.dp,
                                        start = 16.dp,
                                        end = 16.dp,
                                        bottom = 270.dp
                                    )
                                    .clip(
                                        RoundedCornerShape(24.dp)
                                    )
                            )

                            // ------------------------------------------------------------
                            // TITLE
                            // ------------------------------------------------------------

                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .safeDrawingPadding()
                                    .padding(top = 12.dp),

                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = Color(0xFF9CA8FF)
                                )

                                Spacer(
                                    modifier = Modifier.width(8.dp)
                                )

                                Text(
                                    text = "Edit Photo",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // ------------------------------------------------------------
                            // BOTTOM EDITOR PANEL
                            // ------------------------------------------------------------

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter),

                                shape = RoundedCornerShape(
                                    topStart = 28.dp,
                                    topEnd = 28.dp
                                ),

                                color = Color(0xFF1B1D22)
                            ) {

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .navigationBarsPadding()
                                        .padding(
                                            start = 20.dp,
                                            end = 20.dp,
                                            top = 18.dp,
                                            bottom = 16.dp
                                        )
                                ) {

                                    // ----------------------------------------------------
                                    // BRIGHTNESS
                                    // ----------------------------------------------------

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment =
                                            Alignment.CenterVertically
                                    ) {

                                        Icon(
                                            imageVector = Icons.Default.WbSunny,
                                            contentDescription = "Brightness",
                                            tint = Color(0xFF9CA8FF)
                                        )

                                        Spacer(
                                            modifier = Modifier.width(10.dp)
                                        )

                                        Text(
                                            text = "Brightness",
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        Spacer(
                                            modifier = Modifier.weight(1f)
                                        )

                                        Text(
                                            text = brightness.toInt().toString(),
                                            color = Color(0xFFB8BBC4)
                                        )
                                    }

                                    Slider(
                                        value = brightness,

                                        onValueChange = { value ->

                                            brightness = value

                                            processingJob?.cancel()

                                            processingJob = scope.launch {

                                                val result =
                                                    withContext(Dispatchers.Default) {

                                                        imageProcessor.adjustImage(
                                                            bitmap = bitmap,
                                                            brightness =
                                                                value.toDouble(),
                                                            contrast =
                                                                contrast.toDouble()
                                                        )
                                                    }

                                                editedBitmap = result
                                            }
                                        },

                                        valueRange = -100f..100f
                                    )

                                    // ----------------------------------------------------
                                    // CONTRAST
                                    // ----------------------------------------------------

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment =
                                            Alignment.CenterVertically
                                    ) {

                                        Icon(
                                            imageVector = Icons.Default.Contrast,
                                            contentDescription = "Contrast",
                                            tint = Color(0xFF9CA8FF)
                                        )

                                        Spacer(
                                            modifier = Modifier.width(10.dp)
                                        )

                                        Text(
                                            text = "Contrast",
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        Spacer(
                                            modifier = Modifier.weight(1f)
                                        )

                                        Text(
                                            text = String.format(
                                                "%.1fx",
                                                contrast
                                            ),
                                            color = Color(0xFFB8BBC4)
                                        )
                                    }

                                    Slider(
                                        value = contrast,

                                        onValueChange = { value ->

                                            contrast = value

                                            processingJob?.cancel()

                                            processingJob = scope.launch {

                                                val result =
                                                    withContext(Dispatchers.Default) {

                                                        imageProcessor.adjustImage(
                                                            bitmap = bitmap,
                                                            brightness =
                                                                brightness.toDouble(),
                                                            contrast =
                                                                value.toDouble()
                                                        )
                                                    }

                                                editedBitmap = result
                                            }
                                        },

                                        valueRange = 0.5f..2f
                                    )

                                    Spacer(
                                        modifier = Modifier.height(8.dp)
                                    )

                                    // ----------------------------------------------------
                                    // RESET + DONE
                                    // ----------------------------------------------------

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement =
                                            Arrangement.spacedBy(12.dp)
                                    ) {

                                        Surface(
                                            modifier = Modifier.weight(1f),

                                            shape = RoundedCornerShape(18.dp),

                                            color = Color(0xFF272A31),

                                            onClick = {

                                                processingJob?.cancel()

                                                brightness = 0f
                                                contrast = 1f
                                                editedBitmap = null
                                            }
                                        ) {

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(
                                                        vertical = 13.dp
                                                    ),

                                                horizontalArrangement =
                                                    Arrangement.Center,

                                                verticalAlignment =
                                                    Alignment.CenterVertically
                                            ) {

                                                Icon(
                                                    imageVector =
                                                        Icons.Default.Refresh,

                                                    contentDescription = "Reset",

                                                    tint = Color.White
                                                )

                                                Spacer(
                                                    modifier =
                                                        Modifier.width(8.dp)
                                                )

                                                Text(
                                                    text = "Reset",
                                                    color = Color.White,
                                                    fontWeight =
                                                        FontWeight.SemiBold
                                                )
                                            }
                                        }

                                        Surface(
                                            modifier = Modifier.weight(1f),

                                            shape = RoundedCornerShape(18.dp),

                                            color = Color(0xFF7C8CFF),

                                            onClick = {

                                                screenState =
                                                    CameraScreenState.PREVIEW
                                            }
                                        ) {

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(
                                                        vertical = 13.dp
                                                    ),

                                                horizontalArrangement =
                                                    Arrangement.Center,

                                                verticalAlignment =
                                                    Alignment.CenterVertically
                                            ) {

                                                Icon(
                                                    imageVector =
                                                        Icons.Default.Check,

                                                    contentDescription = "Done",

                                                    tint = Color.White
                                                )

                                                Spacer(
                                                    modifier =
                                                        Modifier.width(8.dp)
                                                )

                                                Text(
                                                    text = "Done",
                                                    color = Color.White,
                                                    fontWeight =
                                                        FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
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
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .navigationBarsPadding()
                                    .padding(
                                        start = 20.dp,
                                        end = 20.dp,
                                        bottom = 32.dp
                                    )
                            ) {

                                Button(
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        screenState = CameraScreenState.EDITING
                                    }
                                ) {
                                    Text("Edit")
                                }

                                Spacer(
                                    modifier = Modifier.width(12.dp)
                                )

                                Button(
                                    modifier = Modifier.weight(1f),
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

