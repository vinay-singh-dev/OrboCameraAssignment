package com.example.orbocameraassignment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.orbocameraassignment.camera.CameraScreen
import com.example.orbocameraassignment.ui.theme.OrboCameraAssignmentTheme
import org.opencv.android.OpenCVLoader

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!OpenCVLoader.initLocal()) {
            throw RuntimeException("OpenCV initialization failed")
        }

        enableEdgeToEdge()

        setContent {
            OrboCameraAssignmentTheme {
                CameraScreen()
            }
        }
    }
}