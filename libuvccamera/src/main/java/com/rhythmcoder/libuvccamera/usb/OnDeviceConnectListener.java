package com.rhythmcoder.libuvccamera.usb;

import android.hardware.usb.UsbDevice;

/**
 * Author:create by RhythmCoder
 * Date:2023/12/9
 * Description:
 */
public interface OnDeviceConnectListener {
    /**
     * called when device attached
     *
     * @param device
     */
    public void onAttach(UsbDevice device);

    /**
     * called when device dettach(after onDisconnect)
     *
     * @param device
     */
    public void onDettach(UsbDevice device);

    /**
     * called after device opend
     *
     * @param device
     * @param ctrlBlock
     * @param createNew
     */
    public void onConnect(UsbDevice device);

    /**
     * called when USB device removed or its power off (this callback is called after device closing)
     *
     * @param device
     * @param ctrlBlock
     */
    public void onDisconnect(UsbDevice device);


}
