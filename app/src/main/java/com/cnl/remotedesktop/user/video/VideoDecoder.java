package com.cnl.remotedesktop.user.video;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import com.cnl.remotedesktop.user.network.Server;
import com.cnl.remotedesktop.utils.ToastUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class VideoDecoder {

    private static final String TAG = "VideoDecoder";
    private static final int TIMEOUT_US = 300;
    private OnSizeChangeCallback callback;
    private Surface mSurface;
    private Server mServer;
    private Worker mWorker;

    public VideoDecoder(Surface surface, Server server) {
        mSurface = surface;
        mServer = server;
    }

    public void start() {
        if (mWorker == null) {
            mWorker = new Worker();
            mWorker.setRunning(true);
            mWorker.start();
        }
    }

    public void stop() throws IOException {
        if (mWorker != null) {
            mWorker.setRunning(false);
            mWorker = null;
        }
        if (mServer != null) {
            if (!mServer.hasRelease()) {
                mServer.release();
            }
        }
    }

    public void setCallback(OnSizeChangeCallback callback) {
        this.callback = callback;
    }

    private class Worker extends Thread {
        volatile boolean isRunning;
        MediaCodec.BufferInfo mBufferInfo;
        private MediaCodec decoder;

        boolean prepare() throws IOException {
            mServer.start();
            mBufferInfo = new MediaCodec.BufferInfo();
            int mWidth;
            int mHeight;
            try {
                mWidth = mServer.readInt();
                mHeight = mServer.readInt();
                if (callback != null) {
                    callback.onSizeChange(mWidth, mHeight);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            //编码器那边会先发sps和pps来，头一帧就由sps和pps组成
            byte[] spspps = mServer.readFrame();
            if (spspps == null) {
                return false;
            }
            //找到sps与pps的分隔处
            int pos = 0;
            if (!((pos + 3 < spspps.length) &&
                    (spspps[pos] == 0 &&
                            spspps[pos + 1] == 0 &&
                            spspps[pos + 2] == 0 &&
                            spspps[pos + 3] == 1))) {
                return false;
            } else {
                //00 00 00 01开始标志后的一位
                pos = 4;
            }
            while ((pos + 3 < spspps.length) &&
                    !(spspps[pos] == 0 &&
                            spspps[pos + 1] == 0 &&
                            spspps[pos + 2] == 0 &&
                            spspps[pos + 3] == 1)) {
                pos++;
            }
            if (pos + 3 >= spspps.length) {
                return false;
            }
            byte[] mSps = Arrays.copyOfRange(spspps, 0, pos);
            byte[] mPps = Arrays.copyOfRange(spspps, pos, spspps.length);
            MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, mWidth, mHeight);
            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, mHeight * mWidth);
            format.setInteger(MediaFormat.KEY_MAX_HEIGHT, mHeight);
            format.setInteger(MediaFormat.KEY_MAX_WIDTH, mWidth);
            format.setByteBuffer("csd-0", ByteBuffer.wrap(mSps));
            format.setByteBuffer("csd-1", ByteBuffer.wrap(mPps));
            try {
                decoder = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            } catch (IOException e) {
                e.printStackTrace();
            }
            decoder.configure(format, mSurface, null, 0);
            decoder.start();
            return true;
        }

        void setRunning(boolean running) {
            isRunning = running;
        }

        @Override
        public void run() {
            try {
                if (!prepare()) {
                    Log.d(TAG, "视频解码器初始化失败");
                    ToastUtils.toast("视频解码器初始化失败");
                    isRunning = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (isRunning) {
                try {
                    decode();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            release();
        }

        private void decode() throws IOException {
            boolean isEOS = false;
            while (!isEOS) {
                int inIndex = decoder.dequeueInputBuffer(TIMEOUT_US);
                if (inIndex >= 0) {
                    byte[] frame = mServer.readFrame();
                    ByteBuffer buffer = decoder.getInputBuffer(inIndex);
                    if (buffer == null) continue;
                    buffer.clear();
                    if (frame != null) {
                        buffer.put(frame, 0, frame.length);
                        buffer.clear();
                        buffer.limit(frame.length);
                        decoder.queueInputBuffer(inIndex, 0, frame.length, 0, MediaCodec.BUFFER_FLAG_KEY_FRAME);
                    } else {
                        Log.d(TAG, "InputBuffer BUFFER_FLAG_END_OF_STREAM");
                        decoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        isEOS = true;
                        isRunning = false;
                        mServer.release();
                    }
                } else {
                    isEOS = true;
                }
                int outIndex = decoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_US);
                while (outIndex >= 0) {
                    decoder.releaseOutputBuffer(outIndex, true);
                    outIndex = decoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_US);//再次获取数据，如果没有数据输出则outIndex=-1 循环结束
                }
            }
        }

        private void release() {
            if (decoder != null) {
                decoder.stop();
                decoder.release();
            }
        }
    }

    public interface OnSizeChangeCallback {
        void onSizeChange(int w, int h);
    }

}