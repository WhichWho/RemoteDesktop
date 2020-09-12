package com.cnl.remotedesktop.server.video;


import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.view.Surface;

import com.cnl.remotedesktop.server.network.Server;

import java.io.IOException;
import java.nio.ByteBuffer;


public class VideoEncoder265 {

    private int VIDEO_FRAME_PER_SECOND = 30;
    private int VIDEO_I_FRAME_INTERVAL = 5;
    private int VIDEO_BITRATE = 8 * 1024 * 1024;

    private final String TAG = "VideoEncoder";
    private Worker mWorker;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private Server server;
    private byte[] mFrameByte;

    public VideoEncoder265(MediaProjection mediaProjection, Server client) {
        mMediaProjection = mediaProjection;
        server = client;
    }

    protected void onSurfaceCreated(Surface surface, int mWidth, int mHeight, int dpi) {
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("-display",
                mWidth, mHeight, dpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                surface, null, null);
    }

    protected void onSurfaceDestroyed(Surface surface) {
        mVirtualDisplay.release();
        surface.release();
    }


    public void start(int w, int h, int d) {
        if (mWorker == null) {
            mWorker = new Worker(w, h, d);
            mWorker.setRunning(true);
            mWorker.start();
        }
    }

    public void stop() {
        if (mWorker != null) {
            mWorker.setRunning(false);
            mWorker = null;
        }
    }


    private class Worker extends Thread {
        private final long mTimeoutUsec;
        private MediaCodec.BufferInfo mBufferInfo;
        private MediaCodec mCodec;
        private volatile boolean isRunning;
        private Surface mSurface;
        private int mWidth;
        private int mHeight;
        private int dpi;

        public Worker(int w, int h, int d) {
            mBufferInfo = new MediaCodec.BufferInfo();
            mTimeoutUsec = 10000L;
            mWidth = w;
            mHeight = h;
            dpi = d;
        }

        public void setRunning(boolean running) {
            isRunning = running;
        }

        protected void onEncodedSample(MediaCodec.BufferInfo info, ByteBuffer data) {
            if (mFrameByte == null || mFrameByte.length < info.size) {
                mFrameByte = new byte[info.size];
            }
            data.get(mFrameByte, 0, info.size);
            try {
                server.writeInt(info.size);
                server.write(mFrameByte, 0, info.size);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            if (!prepare()) {
                isRunning = false;
            }
            while (isRunning) {
                encode();
            }
            release();
        }

        void encode() {
            if (!isRunning) {
                //编码结束，发送结束信号，让surface不在提供数据
                mCodec.signalEndOfInputStream();
            }
            int status = mCodec.dequeueOutputBuffer(mBufferInfo, mTimeoutUsec);
            if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                return;
            } else if (status >= 0) {
                ByteBuffer data = mCodec.getOutputBuffer(status);
                if (data != null) {
                    final int endOfStream = mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM;
                    //传递编码数据
                    if (endOfStream == 0) {
                        onEncodedSample(mBufferInfo, data);
                    }
                    // 一定要记得释放
                    mCodec.releaseOutputBuffer(status, false);
                    if (endOfStream == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                        return;
                    }
                }
            }
        }

        private void release() {
            onSurfaceDestroyed(mSurface);
            if (mCodec != null) {
                mCodec.stop();
                mCodec.release();
            }
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private boolean prepare() {
            try {
                server.writeInt(mWidth);
                server.writeInt(mHeight);
            } catch (IOException e) {
                e.printStackTrace();
            }
            MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, mWidth, mHeight);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            format.setInteger(MediaFormat.KEY_BIT_RATE, VIDEO_BITRATE);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, VIDEO_FRAME_PER_SECOND);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, VIDEO_I_FRAME_INTERVAL);
            format.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 40);
            format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CQ);
            try {
                mCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            mCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mSurface = mCodec.createInputSurface();
            mCodec.start();
            onSurfaceCreated(mSurface, mWidth, mHeight, dpi);
            return true;
        }
    }
}