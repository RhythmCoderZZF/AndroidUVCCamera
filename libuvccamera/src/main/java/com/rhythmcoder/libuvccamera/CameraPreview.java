package com.rhythmcoder.libuvccamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Runnable, CameraViewInterface {
    private static final String TAG = CameraPreview.class.getSimpleName();
    private Thread mainLoop;
    private Bitmap bmp;
    static final int IMG_WIDTH = 256;
    static final int IMG_HEIGHT = 192;
    private Rect mSrcRect;
    private Rect mDstRect;
    private volatile boolean isOpen;

    public CameraPreview(Context context) {
        super(context);
        initView();
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CameraPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {
        Log.i(TAG, "CameraPreview constructed");
        setFocusable(true);
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            UsbCameraLib.pixeltobmp(bmp);
            Canvas canvas = getHolder().lockCanvas();
            if (canvas != null) {
                canvas.drawBitmap(bmp, mSrcRect, mDstRect, null);
                getHolder().unlockCanvasAndPost(canvas);
            }
            /*try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated");
        if (bmp == null) {
            bmp = Bitmap.createBitmap(IMG_WIDTH, IMG_HEIGHT, Bitmap.Config.RGB_565);
        }
        mSrcRect = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
        mDstRect = new Rect(0, 0, getWidth(), getHeight());
        mainLoop = new Thread(this);
        mainLoop.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed");
        closeCamera();
        mainLoop.interrupt();
        if (bmp != null) {
            bmp.recycle();
        }
    }

    @Override
    public void openCamera() {
        Log.i(TAG, "openCamera: ");
    }

    @Override
    public void closeCamera() {
        Log.i(TAG, "closeCamera: ");
    }
}
