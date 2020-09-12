package com.cnl.remotedesktop.views;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;


public class PlaySurfaceView extends SurfaceView {

    private OnScaleCallBack callback;
    private Handler hd;

    public PlaySurfaceView(Context context) {
        super(context);
        init(context);
    }

    public PlaySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PlaySurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public PlaySurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        if (hd == null)
            hd = new Handler(context.getMainLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    resizeView(msg.arg1, msg.arg2);
                }
            };
    }

    public void setCallback(OnScaleCallBack callback) {
        this.callback = callback;
    }

    public void resizeView(int remoteWidth, int remoteHeight, boolean callFromMainThread) {
        if (callFromMainThread) {
            resizeView(remoteWidth, remoteHeight);
        } else {
            Message msg = Message.obtain();
            msg.arg1 = remoteWidth;
            msg.arg2 = remoteHeight;
            hd.handleMessage(msg);
        }
    }

    private void resizeView(int remoteWidth, int remoteHeight) {
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        if (viewWidth == 0 || viewHeight == 0) return;
        float scale, dx, dy;

        if (remoteWidth <= viewWidth && remoteHeight <= viewHeight) {
            scale = 1.0f;
        } else {
            scale = Math.min((float) viewWidth / (float) remoteWidth,
                    (float) viewHeight / (float) remoteHeight);
        }

        dx = Math.round((viewWidth - remoteWidth * scale) * 0.5f);
        dy = Math.round((viewHeight - remoteHeight * scale) * 0.5f);

        if (viewHeight != remoteHeight || viewWidth != remoteWidth) {
            FrameLayout.LayoutParams lpm = (FrameLayout.LayoutParams) getLayoutParams();
            lpm.width = (int) (scale * remoteWidth);
            lpm.height = (int) (scale * remoteHeight);
            lpm.leftMargin = (int) dx;
            lpm.topMargin = (int) dy;
        }
//        ToastUtils.toast(String.format("video size: %d * %d", remoteWidth, remoteHeight));
//        ToastUtils.toast(String.format("surface size: %d * %d", viewWidth, viewHeight));
//        ToastUtils.toast(String.format("resized view %d * %d", lpm.width, lpm.height));
        if (callback != null) {
            callback.onScale(scale);
        }
    }

    public interface OnScaleCallBack {
        void onScale(float scale);
    }
}
