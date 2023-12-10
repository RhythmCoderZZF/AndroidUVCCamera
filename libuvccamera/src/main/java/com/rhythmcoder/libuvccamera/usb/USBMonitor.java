package com.rhythmcoder.libuvccamera.usb;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Author:create by RhythmCoder
 * Date:2023/12/9
 * Description:
 * Listen to some USB broadcasts and request usb connection permission.
 */
public class USBMonitor {
    private static final String TAG = "USBMonitor";
    private static final String DEFAULT_USBFS = "/dev/bus/usb";
    private final WeakReference<Context> mWeakContext;
    private PendingIntent mPermissionIntent = null;
    private static final String ACTION_USB_PERMISSION = "com.zzf.usb.action.USB_PERMISSION";
    private UsbManager mUsbManager;
    private final OnDeviceConnectListener mOnDeviceConnectListener;
    public UsbControlBlock mUsbControlBlock;


    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive<< action:" + action);
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (ACTION_USB_PERMISSION.equals(action)) {
                boolean hasPermission = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                Log.d(TAG, "EXTRA_PERMISSION_GRANTED:" + hasPermission);
                if (hasPermission) {
                    if (device != null) {
                        processConnect(device);
                    }
                } else {
                    Toast.makeText(mWeakContext.get(), "DENIED to access USB permission...", Toast.LENGTH_SHORT).show();
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                processAttach(device);
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                processDettach(device);
            }
        }
    };


    public USBMonitor(Context context, final OnDeviceConnectListener listener) {
        mWeakContext = new WeakReference<Context>(context);
        mOnDeviceConnectListener = listener;
        mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
    }


    public void register() {
        Log.d(TAG, "register<<");
        if (mPermissionIntent == null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                mPermissionIntent = PendingIntent.getBroadcast(mWeakContext.get(), 123, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_MUTABLE);
            } else {
                mPermissionIntent = PendingIntent.getBroadcast(mWeakContext.get(), 123, new Intent(ACTION_USB_PERMISSION), 0);
            }
            final IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            mWeakContext.get().registerReceiver(mUsbReceiver, filter);
        }

        List<UsbDevice> listDevices = getDeviceList();
        UsbDevice device = listDevices.size() > 0 ? listDevices.get(0) : null;
        if (device != null) {
            requestPermission(device);
        }
    }

    public void unRegister() {
        Log.d(TAG, "unRegister<<");
        mWeakContext.get().unregisterReceiver(mUsbReceiver);
        mPermissionIntent = null;
    }

    private void requestPermission(final UsbDevice device) {
        Log.d(TAG, "requestPermission<< device:" + device.getDeviceName());
        if (device != null) {
            if (mUsbManager.hasPermission(device)) {
                Log.d(TAG, "already get permission to access usb device");
                processConnect(device);
            } else {
                try {
                    Log.d(TAG, "need to request permission to access usb device");
                    mUsbManager.requestPermission(device, mPermissionIntent);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<UsbDevice> getDeviceList() {
        final HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        final List<UsbDevice> result = new ArrayList<>();
        if (deviceList != null) {
            for (final UsbDevice device : deviceList.values()) {
                result.add(device);
            }
        }
        return result;
    }

    private void processConnect(final UsbDevice device) {
        mUsbControlBlock = new UsbControlBlock(this, device);
        Log.d(TAG, "processConnect<< UsbControlBlock:" + mUsbControlBlock);
        if (mOnDeviceConnectListener != null) {
            mOnDeviceConnectListener.onConnect(device);
        }
    }

    private void processAttach(final UsbDevice device) {
        requestPermission(device);
        if (mOnDeviceConnectListener != null) {
            mOnDeviceConnectListener.onAttach(device);
        }
    }

    private void processDettach(final UsbDevice device) {
        if (mOnDeviceConnectListener != null) {
            mOnDeviceConnectListener.onDettach(device);
        }
    }

    public class UsbControlBlock {
        private final WeakReference<USBMonitor> mWeakMonitor;
        private final WeakReference<UsbDevice> mWeakDevice;
        private UsbDeviceConnection mConnection;
        private final int mBusNum;
        private final int mDevNum;

        private UsbControlBlock(final USBMonitor monitor, final UsbDevice device) {
            mWeakMonitor = new WeakReference<USBMonitor>(monitor);
            mWeakDevice = new WeakReference<UsbDevice>(device);
            mConnection = monitor.mUsbManager.openDevice(device);
            final String name = device.getDeviceName();
            final String[] v = !TextUtils.isEmpty(name) ? name.split("/") : null;
            int busnum = 0;
            int devnum = 0;
            if (v != null) {
                busnum = Integer.parseInt(v[v.length - 2]);
                devnum = Integer.parseInt(v[v.length - 1]);
            }
            mBusNum = busnum;
            mDevNum = devnum;
        }

        public int getVendorId() {
            final UsbDevice device = mWeakDevice.get();
            return device != null ? device.getVendorId() : 0;
        }

        /**
         * get product id
         *
         * @return
         */
        public int getProductId() {
            final UsbDevice device = mWeakDevice.get();
            return device != null ? device.getProductId() : 0;
        }

        public synchronized int getFileDescriptor() {
            return mConnection.getFileDescriptor();
        }

        public synchronized byte[] getRawDescriptors() {
            return mConnection.getRawDescriptors();
        }

        public int getBusNum() {
            return mBusNum;
        }

        public int getDevNum() {
            return mDevNum;
        }

        public String getDeviceName() {
            final UsbDevice device = mWeakDevice.get();
            return device != null ? device.getDeviceName() : "";
        }

        public final String getUSBFSName() {
            String result = null;
            final String name = getDeviceName();
            final String[] v = !TextUtils.isEmpty(name) ? name.split("/") : null;
            if ((v != null) && (v.length > 2)) {
                final StringBuilder sb = new StringBuilder(v[0]);
                for (int i = 1; i < v.length - 2; i++)
                    sb.append("/").append(v[i]);
                result = sb.toString();
            }
            if (TextUtils.isEmpty(result)) {
                Log.w(TAG, "failed to get USBFS path, try to use default path:" + name);
                result = DEFAULT_USBFS;
            }
            return result;
        }

        @Override
        public String toString() {
            return "UsbControlBlock{" +
                    ", mWeakDevice=" + mWeakDevice.toString() +
                    ", mConnection=" + mConnection.getFileDescriptor() +
                    ", mBusNum=" + mBusNum +
                    ", mDevNum=" + mDevNum +
                    '}';
        }
    }

}
