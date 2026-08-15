#include <jni.h>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_orbocameraassignment_NativeImageProcessor_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {

    return env->NewStringUTF("Native bridge is alive");
}