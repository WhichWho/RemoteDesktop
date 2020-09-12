package com.cnl.remotedesktop.server.audio;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import com.cnl.remotedesktop.config.AudioConfig;

import java.io.IOException;
import java.nio.ByteBuffer;


public class AacEncoder {

    private MediaCodec mediaCodec;
    private MediaCodec.BufferInfo bufferInfo;
    private long presentationTimeUs = 0;
    private byte[] ADTSBuffer;
    private int freqIdx;
    private int chanCfg;

    public AacEncoder(AudioConfig config) throws IOException {
        init(config);
    }

    private void init(AudioConfig config) throws IOException {
        MediaFormat mediaFormat = MediaFormat.createAudioFormat(config.getFormat(),
                config.getFrequency(), config.getChannelCount());
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, config.getProfile());
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, config.getBitrate());
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 10240);
        mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mediaCodec.start();
        bufferInfo = new MediaCodec.BufferInfo();
        ADTSBuffer = new byte[7];
        freqIdx = config.getAACFrequencyIdx();
        chanCfg = config.getChannelCount();
        Log.e("AACEncoder", String.format("freqIdx: %d, chanCfg: %d", freqIdx, chanCfg));
    }

    public void close() {
        mediaCodec.stop();
        mediaCodec.release();
    }

    public void encode(ByteBuffer input, ByteBuffer output) {

        int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);//其中需要注意的有dequeueInputBuffer（-1），参数表示需要得到的毫秒数，-1表示一直等，0表示不需要等，传0的话程序不会等待，但是有可能会丢帧。
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex);
            if (inputBuffer == null) return;
            inputBuffer.clear();
            inputBuffer.put(input);
            inputBuffer.limit(input.limit());

            //计算pts
            long pts = computePresentationTime(presentationTimeUs);

            mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.limit(), pts, 0);
            presentationTimeUs += 1;
        }

        output.clear();
        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
        while (outputBufferIndex >= 0) {
            int outBitsSize = bufferInfo.size;
            int outPacketSize = outBitsSize + 7; // 7 is ADTS size
            ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex);
            if (outputBuffer == null) return;
            outputBuffer.position(bufferInfo.offset);
            outputBuffer.limit(bufferInfo.offset + outBitsSize);

            addADTStoPacket(ADTSBuffer, outPacketSize);

            output.put(ADTSBuffer);
            output.put(outputBuffer);
            outputBuffer.position(bufferInfo.offset);

            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
        }

        output.limit(output.position());
        output.rewind();
    }

    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2;  //AAC LC
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF1;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }

    //计算PTS，实际上这个pts对应音频来说作用并不大，设置成0也是没有问题的
    private long computePresentationTime(long frameIndex) {
        return frameIndex * 90000 * 1024 / 44100;
    }
}