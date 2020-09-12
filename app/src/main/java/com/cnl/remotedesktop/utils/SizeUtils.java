package com.cnl.remotedesktop.utils;

import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaCodec;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.WindowManager;

public class SizeUtils {

    public static int width;
    public static int height;
    public static int dpi;
    public static String cameraId;

    public static void setContextAsScreen(Context ctx) {
        WindowManager wm = ctx.getSystemService(WindowManager.class);
        if (wm == null) return;
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;
        dpi = dm.densityDpi;
    }

    public static void setContextAsCamera(Context ctx) {
        CameraManager manager = ctx.getSystemService(CameraManager.class);
        if (manager == null) return;
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                Integer tag = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (tag == null || tag == CameraCharacteristics.LENS_FACING_FRONT) continue;
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) continue;
                Size data = getOptimalSize(map.getOutputSizes(MediaCodec.class));
                width = data.getWidth();
                height = data.getHeight();
                SizeUtils.cameraId = cameraId;
                break;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private static Size getOptimalSize(Size[] outputSizes) {
        Size max = outputSizes[0];
        int pixels = max.getWidth() * max.getHeight();
        for (Size sizei : outputSizes) {
            int pixeli = sizei.getWidth() * sizei.getHeight();
            if (pixeli > pixels) {
                max = sizei;
                pixels = pixeli;
            }
        }
        return max;
    }

}
