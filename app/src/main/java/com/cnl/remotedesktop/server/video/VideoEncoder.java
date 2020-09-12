package com.cnl.remotedesktop.server.video;


import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import com.cnl.remotedesktop.config.VideoConfig;
import com.cnl.remotedesktop.server.network.Server;
import com.cnl.remotedesktop.server.video.source.VideoSource;

import java.io.IOException;
import java.nio.ByteBuffer;


public class VideoEncoder {

    private VideoSource source;
    private Server output;
    private VideoConfig config;
    private Worker mWorker;

    public VideoEncoder(VideoSource source, Server output, VideoConfig config) {
        this.source = source;
        this.output = output;
        this.config = config;
    }

    private void onSurfaceCreated(Surface surface, int mWidth, int mHeight, int dpi) {
        source.init(surface, mWidth, mHeight, dpi);
    }

    private void onSurfaceDestroyed(Surface surface) {
        source.release();
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

        private byte[] mFrameByte;
        private MediaCodec.BufferInfo mBufferInfo;
        private MediaCodec mCodec;
        private volatile boolean isRunning;
        private Surface mSurface;
        private int mWidth;
        private int mHeight;
        private int dpi;

        Worker(int w, int h, int d) {
            mBufferInfo = new MediaCodec.BufferInfo();
            mWidth = w;
            mHeight = h;
            dpi = d;
        }

        @Override
        public void run() {
            try {
                prepare();
            } catch (IOException e) {
                e.printStackTrace();
                isRunning = false;
            }
            while (isRunning) {
                encode();
            }
            release();
        }

        void setRunning(boolean running) {
            isRunning = running;
        }

        void onEncodedSample(MediaCodec.BufferInfo info, ByteBuffer data) {
            if (mFrameByte == null || mFrameByte.length < info.size) {
                mFrameByte = new byte[info.size];
            }
            data.get(mFrameByte, 0, info.size);
            try {
                output.writeInt(info.size);
                output.write(mFrameByte, 0, info.size);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void encode() {
            if (!isRunning) {
                mCodec.signalEndOfInputStream();
            }
            int status = mCodec.dequeueOutputBuffer(mBufferInfo, 10000);
            if (status < 0) return;
            ByteBuffer data = mCodec.getOutputBuffer(status);
            if (data == null) return;
            final int endOfStream = mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM;
            if (endOfStream == 0) {
                onEncodedSample(mBufferInfo, data);
            }
            mCodec.releaseOutputBuffer(status, false);
            if (endOfStream == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                //自然结束(基本不可能)
                Log.e("VideoEncoder", "BUFFER_FLAG_END_OF_STREAM");
            }
        }

        private void release() {
            onSurfaceDestroyed(mSurface);
            if (mCodec != null) {
                mCodec.stop();
                mCodec.release();
            }
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void prepare() throws IOException {
            output.writeInt(mWidth);
            output.writeInt(mHeight);
            MediaFormat format = MediaFormat.createVideoFormat(config.getFormat(), mWidth, mHeight);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            format.setInteger(MediaFormat.KEY_BIT_RATE, config.getBitrate());
            format.setInteger(MediaFormat.KEY_FRAME_RATE, config.getFps());
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, config.getiFrame());
            format.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 40);
            mCodec = MediaCodec.createEncoderByType(config.getFormat());
            mCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mSurface = mCodec.createInputSurface();
            mCodec.start();
            onSurfaceCreated(mSurface, mWidth, mHeight, dpi);
        }
    }
}