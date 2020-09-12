package com.cnl.remotedesktop.user.audio;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import com.cnl.remotedesktop.config.AudioConfig;
import com.cnl.remotedesktop.user.network.Server;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AacDecoder extends Thread {
    private static final String TAG = "AacDecoder";

    private static final long kTimeOutUs = 5000;
    private MediaCodec codec;
    private MediaCodec.BufferInfo info;
    private boolean sawInputEOS = false;
    private boolean sawOutputEOS = false;
    private Server input;
    private AudioConfig config;
    private boolean first = false;

    public AacDecoder(Server input, AudioConfig config) {
        this.input = input;
        this.config = config;
    }

    private void init(AudioConfig config) throws IOException {
        info = new MediaCodec.BufferInfo();
        MediaFormat mediaFormat = MediaFormat.createAudioFormat(config.getFormat(),
                config.getFrequency(), config.getChannelCount());
        mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, config.getChannel());
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 10240);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, config.getBitrate());
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, config.getProfile());
        mediaFormat.setInteger(MediaFormat.KEY_IS_ADTS, 0);

//        AAC Profile 5bits | 采样率 4bits | 声道数 4bits | 其他 3bits  但是不加好像也没问题
//        https://blog.csdn.net/lavender1626/article/details/80431902
//        byte[] data = new byte[]{(byte) 0x12, (byte) 0x10};
//        ByteBuffer csd_0 = ByteBuffer.wrap(data);
//        mediaFormat.setByteBuffer("csd-0", csd_0);

        codec = MediaCodec.createDecoderByType(config.getFormat());
        codec.configure(mediaFormat, null, null, 0);
        codec.start();
    }

    /**
     * packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
     * packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
     * packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
     */
    private int getFrameSize(ByteBuffer bb) {
        int len = 0;
        byte[] head = bb.array();
        if ((head[0] & 0xFF) != 0xFF || (head[1] & 0xF0) != 0xF0)
            throw new RuntimeException("NOT ADTS header!");
        len += (head[3] & 0b1111) << 11;
        len += (head[4] & 0xFF) << 3;
        len += (head[5] & 0xFF) >> 5;
        return len - 7;
    }

    private int getFrequency(ByteBuffer bb) {
        byte[] aacFrame = bb.array();
        int idxFrequency = aacFrame[2] & 0x3C;
        idxFrequency >>= 2;
        return AudioConfig.conventAACFrequency(idxFrequency);
    }

    @Override
    public void run() {
        try {
            Log.e("AacDecoder", "audio AACDecoder running!");
            AudioTrackPlayer player = new AudioTrackPlayer();
            ByteBuffer bb = ByteBuffer.allocate(7);
            player.init(config);
            init(config);
            input.start();
            Log.e("AacDecoder", "audio input.start");
            while (!sawOutputEOS && !sawInputEOS && !isInterrupted()) {
                int inIndex = codec.dequeueInputBuffer(kTimeOutUs);
                if (inIndex >= 0) {
                    bb.clear();
                    bb.put(input.read(bb.limit()));
                    int sampleSize = getFrameSize(bb);
//                    Log.e(TAG, "frameSize " + sampleSize);
                    ByteBuffer dstBuf = codec.getInputBuffer(inIndex);
                    if (dstBuf == null) continue;
                    dstBuf.clear();
                    dstBuf.limit(sampleSize);
                    dstBuf.put(input.read(sampleSize));
                    if (sampleSize < 0) {
                        sawInputEOS = true;
                        codec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    } else {
                        codec.queueInputBuffer(inIndex, 0, sampleSize, 0, 0);
                    }
                }

                int outIndex = codec.dequeueOutputBuffer(info, kTimeOutUs);
//                Log.e(TAG, "codec.dequeueOutputBuffer: index = " + outIndex);
                while (outIndex >= 0) {
                    if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        codec.releaseOutputBuffer(outIndex, false);
                        break;
                    }
                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        sawOutputEOS = true;
                    }
                    if (info.size != 0) {
                        ByteBuffer outBuf = codec.getOutputBuffer(outIndex);
                        if (outBuf == null) continue;
                        outBuf.position(info.offset);
                        outBuf.limit(info.offset + info.size);
                        player.play(outBuf, outBuf.limit());
                        outBuf.position(info.offset);
                    }
                    codec.releaseOutputBuffer(outIndex, false);
                    outIndex = codec.dequeueOutputBuffer(info, kTimeOutUs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (codec != null) {
                codec.stop();
                codec.release();
            } else {
                Log.e(TAG, "run: " + "codec is null");
            }
        }
    }

    public void release() {
        sawOutputEOS = true;
    }

}
