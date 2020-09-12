package com.cnl.remotedesktop.server.video.source;

import android.view.Surface;

public interface VideoSource {

    void init(Surface surface, int mWidth, int mHeight, int dpi);

    void release();

}
