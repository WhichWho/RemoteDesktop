package com.cnl.remotedesktop.server.video.source;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.view.Surface;

import com.cnl.remotedesktop.utils.SizeUtils;

public class ScreenVideoSource implements VideoSource {

    private MediaProjection projection;
    private VirtualDisplay display;

    public ScreenVideoSource(Context ctx, Intent request){
        SizeUtils.setContextAsScreen(ctx);
        MediaProjectionManager maim = ctx.getSystemService(MediaProjectionManager.class);
        if(maim == null) return;
        projection = maim.getMediaProjection(Activity.RESULT_OK, request);
    }

    @Override
    public void init(Surface surface, int mWidth, int mHeight, int dpi){
        display = projection.createVirtualDisplay("-display",
                mWidth, mHeight, dpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                surface, null, null);
    }

    @Override
    public void release(){
        display.release();
    }

}
