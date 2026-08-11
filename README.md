## Overview

Orbo Camera is an Android camera application developed as part of the Android Camera Application assignment.

The application uses the CameraView library to provide a camera preview with a real-time black-and-white filter. Captured images are saved to device storage using Android MediaStore.

OpenCV is integrated to allow the user to manually select an area of the captured image and crop it. The cropped result is then displayed to the user.

## Features

- Camera preview using CameraView
- Real-time black-and-white camera filter
- Image capture
- Saving captured images to device storage
- Camera permission handling
- Manual rectangular crop selection
- OpenCV-based image cropping
- Cropped image preview
- Retake functionality
- Image-save error handling
- Navigation-bar inset handling

## Tech Stack

- Kotlin
- Jetpack Compose
- CameraView 2.7.2
- OpenCV 4.13.0
- Android MediaStore
- AndroidX

## Requirements

- Android Studio
- Android SDK
- Android device or emulator with camera support
- Camera permission
- Internet connection for the initial Gradle dependency download


## Setup

1. Clone the repository.

2. Open the project in Android Studio.

3. Allow Android Studio to sync the Gradle dependencies.

4. Build the project.

5. Run the application on an Android device or emulator.

6. Grant camera permission when prompted.

## How to Use

1. Launch the application.
2. Grant camera permission.
3. The camera preview appears with the black-and-white filter.
4. Tap **Capture** to capture an image.
5. The captured filtered image is saved to device storage.
6. Tap **Crop** to enter cropping mode.
7. Drag across the image to select the area to crop.
8. Tap **Crop** to process the selected region using OpenCV.
9. The cropped image is displayed.
10. Tap **Retake** to return to the camera.

## Project Structure

```text
camera/
├── CameraScreen.kt
├── CameraScreenState.kt
├── CameraViewController.kt
├── CropSelection.kt
└── ImageStorageManager.kt

image/
└── ImageProcessor.kt

MainActivity.kt
```

```markdown
## Image Processing Flow

The image processing pipeline works as follows:

CameraView
↓
Captured Bitmap
↓
Manual crop selection
↓
Screen coordinates converted to Bitmap coordinates
↓
Bitmap converted to OpenCV Mat
↓
OpenCV Rect created from the selected region
↓
Mat.submat() extracts the selected region
↓
Cropped Mat converted back to Bitmap
↓
Cropped image displayed in Compose
```

## Image Storage

Captured images are saved using Android MediaStore.

On Android 10 and above, images are stored under:

Pictures/OrboCamera

The application uses `IS_PENDING` while writing the image and marks the file as complete after successful writing.

Captured images use JPEG format with 90% compression quality.

The captured filtered image is persisted to device storage. The cropped image is generated and displayed to the user.

## Error Handling

The application handles camera permission denial and image-storage failures.

When saving an image:

- MediaStore insertion failure is handled.
- Output-stream failure is handled.
- JPEG compression failure is handled.
- Partially created MediaStore entries are deleted when saving fails.
- The UI displays an error message when the image cannot be saved.

Crop operations also validate that the selected region has a valid width and height before processing.


## Testing

The application was tested on a physical Android device.

The following flows were tested:

- Camera permission grant and denial
- Black-and-white camera preview
- Image capture
- Multiple consecutive captures
- Image persistence in the Gallery
- Manual crop selection
- Cropping from different regions
- Reverse-direction crop selection
- Invalid/small crop selection
- Crop result display
- Retake flow
- Capture after returning from cropping
- Navigation-bar positioning

## Known Limitations

- Brightness and contrast adjustment are not implemented because they are optional bonus features.

