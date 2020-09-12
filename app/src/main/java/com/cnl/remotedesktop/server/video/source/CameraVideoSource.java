package com.cnl.remotedesktop.server.video.source;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.cnl.remotedesktop.utils.SizeUtils;
import com.cnl.remotedesktop.utils.ToastUtils;

import java.util.Collections;

public class CameraVideoSource implements VideoSource {

    private Context ctx;
    private Surface mPreviewSurface;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest mPreviewRequest;
    private CaptureRequest.Builder mPreviewRequestBuilder;

    public CameraVideoSource(Context ctx) {
        SizeUtils.setContextAsCamera(ctx);
        this.ctx = ctx;
    }

    @Override
    public void init(Surface surface, int mWidth, int mHeight, int dpi) {
        mPreviewSurface = surface;
        //旋转方向问题暂时忽略
//        configureTransform(mWidth, mHeight);
        ToastUtils.toast("开启摄像头！");
        openCamera();
    }

    @Override
    public void release() {

    }

    private void openCamera() {
        CameraManager manager = ctx.getSystemService(CameraManager.class);
        if (manager == null) return;
        try {
            if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            HandlerThread thread = new HandlerThread("Camera_front");
            thread.start();
            Handler hd = new Handler(thread.getLooper());
            manager.openCamera(SizeUtils.cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    mCameraDevice = camera;
                    ToastUtils.toast("开始预览");
                    startPreview();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                }
            }, hd);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startPreview() {
        try {
            getPreviewRequestBuilder();
            ToastUtils.toast("预览请求完成");
            mCameraDevice.createCaptureSession(Collections.singletonList(mPreviewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mCaptureSession = session;
                    mPreviewRequestBuilder.setTag("TAG_PREVIEW");
                    mPreviewRequest = mPreviewRequestBuilder.build();
                    try {
                        ToastUtils.toast("循环输出");
                        mCaptureSession.setRepeatingRequest(mPreviewRequest, null, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void getPreviewRequestBuilder() {
        try {
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        mPreviewRequestBuilder.addTarget(mPreviewSurface);
        MeteringRectangle[] meteringRectangles = mPreviewRequestBuilder.get(CaptureRequest.CONTROL_AF_REGIONS);
        if (meteringRectangles != null && meteringRectangles.length > 0) {
            Log.d("Camera2", "PreviewRequestBuilder: AF_REGIONS=" + meteringRectangles[0].getRect().toString());
        }
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE);
    }

}
