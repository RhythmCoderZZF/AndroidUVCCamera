package com.rhythmcoder.libuvccamera;

import android.graphics.Bitmap;

public class UsbCameraLib {

    static {
        System.loadLibrary("myuvccamera");
    }

    public static native int connect(int venderId, int productId, int fileDescriptor, int busNum, int devAddr, String usbfs);

    public static native void release();

    public static native void setStreamMode(int streamMode);

    public static native void pixeltobmp(Bitmap bitmap);

}
