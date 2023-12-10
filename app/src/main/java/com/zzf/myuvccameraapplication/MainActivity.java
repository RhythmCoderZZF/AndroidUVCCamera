package com.zzf.myuvccameraapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.rhythmcoder.libuvccamera.CameraPreview;
import com.rhythmcoder.libuvccamera.UsbCameraManager;
import com.rhythmcoder.libuvccamera.usb.OnDeviceConnectListener;
import com.zzf.myuvccameraapplication.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements OnDeviceConnectListener {

    private final int WIDTH = 256;
    private final int HEIGHT = 192;
    private final int STREAM_MODE = 0;//YUYV


    private ActivityMainBinding binding;
    private UsbCameraManager mManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mManager = UsbCameraManager.getInstance();
        mManager.init(this.getApplicationContext(), this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mManager.register();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mManager.unregister();
    }

    @Override
    public void onAttach(UsbDevice device) {
        Toast.makeText(this, "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDettach(UsbDevice device) {
        Toast.makeText(this, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnect(UsbDevice device) {
        mManager.openCamera(device, binding.cameraPreview);
    }

    @Override
    public void onDisconnect(UsbDevice device) {
        mManager.closeCamera(device, binding.cameraPreview);
    }
}