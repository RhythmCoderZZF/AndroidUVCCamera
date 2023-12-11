#include "uvc_camera.hpp"

extern "C"
JNIEXPORT jint JNICALL
Java_com_rhythmcoder_libuvccamera_UsbCameraLib_connect(
        JNIEnv *env, jclass thiz, jint vid, jint pid, jint fd, jint busNum,
        jint devAddr, jstring usbfs_str) {
    const char *c = env->GetStringUTFChars(usbfs_str, JNI_FALSE);
    std::string c_usbfs = std::string(c);
    env->ReleaseStringUTFChars(usbfs_str, c);
    return connect(0,vid, pid, fd, busNum, devAddr, c_usbfs);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_rhythmcoder_libuvccamera_UsbCameraLib_release(
        JNIEnv *env, jclass thiz) {
    release(0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_rhythmcoder_libuvccamera_UsbCameraLib_setStreamMode(
        JNIEnv *env, jclass thiz, jint mode) {
    set_stream_mode(mode);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_rhythmcoder_libuvccamera_UsbCameraLib_pixeltobmp(
        JNIEnv *env, jclass thiz, jobject bitmap) {
    pixel_to_bmp(env, thiz, 0, bitmap);
}
