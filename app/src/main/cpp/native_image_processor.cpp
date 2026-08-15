#include <jni.h>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <android/bitmap.h>

extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_orbocameraassignment_NativeImageProcessor_crop(
        JNIEnv* env,
        jobject /* this */,
        jobject bitmap,
        jint x,
        jint y,
        jint width,
        jint height
) {
    AndroidBitmapInfo info;

    if (AndroidBitmap_getInfo(env, bitmap, &info)
        != ANDROID_BITMAP_RESULT_SUCCESS) {
        return nullptr;
    }

    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        return nullptr;
    }

    if (x < 0 ||
        y < 0 ||
        width <= 0 ||
        height <= 0 ||
        x + width > static_cast<jint>(info.width) ||
        y + height > static_cast<jint>(info.height)) {
        return nullptr;
    }

    void* inputPixels = nullptr;

    if (AndroidBitmap_lockPixels(env, bitmap, &inputPixels)
        != ANDROID_BITMAP_RESULT_SUCCESS) {
        return nullptr;
    }

    cv::Mat source(
            static_cast<int>(info.height),
            static_cast<int>(info.width),
            CV_8UC4,
            inputPixels,
            info.stride
    );

    cv::Rect cropRect(
            x,
            y,
            width,
            height
    );

    cv::Mat cropped = source(cropRect).clone();

    AndroidBitmap_unlockPixels(env, bitmap);

    jclass bitmapClass =
            env->FindClass("android/graphics/Bitmap");

    if (bitmapClass == nullptr) {
        return nullptr;
    }

    jclass configClass =
            env->FindClass("android/graphics/Bitmap$Config");

    if (configClass == nullptr) {
        return nullptr;
    }

    jmethodID createBitmapMethod = env->GetStaticMethodID(
            bitmapClass,
            "createBitmap",
            "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;"
    );

    if (createBitmapMethod == nullptr) {
        return nullptr;
    }

    jfieldID argb8888Field = env->GetStaticFieldID(
            configClass,
            "ARGB_8888",
            "Landroid/graphics/Bitmap$Config;"
    );

    if (argb8888Field == nullptr) {
        return nullptr;
    }

    jobject argb8888 = env->GetStaticObjectField(
            configClass,
            argb8888Field
    );

    jobject outputBitmap = env->CallStaticObjectMethod(
            bitmapClass,
            createBitmapMethod,
            width,
            height,
            argb8888
    );

    if (outputBitmap == nullptr) {
        return nullptr;
    }

    void* outputPixels = nullptr;

    if (AndroidBitmap_lockPixels(env, outputBitmap, &outputPixels)
        != ANDROID_BITMAP_RESULT_SUCCESS) {
        return nullptr;
    }

    AndroidBitmapInfo outputInfo;

    if (AndroidBitmap_getInfo(env, outputBitmap, &outputInfo)
        != ANDROID_BITMAP_RESULT_SUCCESS) {
        AndroidBitmap_unlockPixels(env, outputBitmap);
        return nullptr;
    }

    cv::Mat destination(
            height,
            width,
            CV_8UC4,
            outputPixels,
            outputInfo.stride
    );

    cropped.copyTo(destination);

    AndroidBitmap_unlockPixels(env, outputBitmap);

    return outputBitmap;
}


extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_orbocameraassignment_NativeImageProcessor_adjustImage(
        JNIEnv* env,
        jobject /* this */,
        jobject bitmap,
        jdouble brightness,
        jdouble contrast
) {
    AndroidBitmapInfo info;

    if (AndroidBitmap_getInfo(env, bitmap, &info)
        != ANDROID_BITMAP_RESULT_SUCCESS) {
        return nullptr;
    }

    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        return nullptr;
    }

    void* inputPixels = nullptr;

    if (AndroidBitmap_lockPixels(env, bitmap, &inputPixels)
        != ANDROID_BITMAP_RESULT_SUCCESS) {
        return nullptr;
    }

    cv::Mat source(
            static_cast<int>(info.height),
            static_cast<int>(info.width),
            CV_8UC4,
            inputPixels,
            info.stride
    );

    cv::Mat result;

    source.convertTo(
            result,
            -1,
            contrast,
            brightness
    );

    AndroidBitmap_unlockPixels(env, bitmap);

    jclass bitmapClass =
            env->FindClass("android/graphics/Bitmap");

    if (bitmapClass == nullptr) {
        return nullptr;
    }

    jclass configClass =
            env->FindClass("android/graphics/Bitmap$Config");

    if (configClass == nullptr) {
        return nullptr;
    }

    jmethodID createBitmapMethod = env->GetStaticMethodID(
            bitmapClass,
            "createBitmap",
            "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;"
    );

    if (createBitmapMethod == nullptr) {
        return nullptr;
    }

    jfieldID argb8888Field = env->GetStaticFieldID(
            configClass,
            "ARGB_8888",
            "Landroid/graphics/Bitmap$Config;"
    );

    if (argb8888Field == nullptr) {
        return nullptr;
    }

    jobject argb8888 = env->GetStaticObjectField(
            configClass,
            argb8888Field
    );

    jobject outputBitmap = env->CallStaticObjectMethod(
            bitmapClass,
            createBitmapMethod,
            static_cast<jint>(info.width),
            static_cast<jint>(info.height),
            argb8888
    );

    if (outputBitmap == nullptr) {
        return nullptr;
    }

    void* outputPixels = nullptr;

    if (AndroidBitmap_lockPixels(env, outputBitmap, &outputPixels)
        != ANDROID_BITMAP_RESULT_SUCCESS) {
        return nullptr;
    }

    AndroidBitmapInfo outputInfo;

    if (AndroidBitmap_getInfo(env, outputBitmap, &outputInfo)
        != ANDROID_BITMAP_RESULT_SUCCESS) {
        AndroidBitmap_unlockPixels(env, outputBitmap);
        return nullptr;
    }

    cv::Mat destination(
            static_cast<int>(outputInfo.height),
            static_cast<int>(outputInfo.width),
            CV_8UC4,
            outputPixels,
            outputInfo.stride
    );

    result.copyTo(destination);

    AndroidBitmap_unlockPixels(env, outputBitmap);

    return outputBitmap;
}