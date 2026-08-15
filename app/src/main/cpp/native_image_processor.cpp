#include <jni.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_orbocameraassignment_NativeImageProcessor_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {

    return env->NewStringUTF("Native bridge is alive");
}