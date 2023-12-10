package com.rhythmcoder.libuvccamera;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.rhythmcoder.libuvccamera.usb.OnDeviceConnectListener;
import com.rhythmcoder.libuvccamera.usb.USBMonitor;

/**
 * Author:create by RhythmCoder
 * Date:2023/12/10
 * Description:
 */
public class UsbCameraManager {
    private static UsbCameraManager sUsbCameraManager;

    private USBMonitor mUSBMonitor;

    private UsbCameraManager() {
    }

    /*PUBLIC*/
    public static UsbCameraManager getInstance() {
        if (sUsbCameraManager == null) {
            sUsbCameraManager = new UsbCameraManager();
        }
        return sUsbCameraManager;
    }

    public void init(Context context, final OnDeviceConnectListener listener) {
        mUSBMonitor = new USBMonitor(context, listener);
    }

    public void register() {
        if (mUSBMonitor != null) mUSBMonitor.register();
    }

    public void unregister() {
        if (mUSBMonitor != null) mUSBMonitor.unRegister();
    }

    public boolean openCamera(UsbDevice dev, CameraViewInterface cameraView) {
        UsbCameraLib.setStreamMode(0);//YUYV
        int result = UsbCameraLib.connect(mUSBMonitor.mUsbControlBlock.getVendorId(), mUSBMonitor.mUsbControlBlock.getProductId(), mUSBMonitor.mUsbControlBlock.getFileDescriptor(), mUSBMonitor.mUsbControlBlock.getBusNum(), mUSBMonitor.mUsbControlBlock.getDevNum(), mUSBMonitor.mUsbControlBlock.getUSBFSName());

        if (result == 0) {
            if (cameraView != null) {
                cameraView.openCamera();
            }
            return true;
        }
        return false;
    }

    public void closeCamera(UsbDevice dev, CameraViewInterface cameraView) {
        if (cameraView != null) {
            cameraView.closeCamera();
        }
        UsbCameraLib.release();
    }

}
