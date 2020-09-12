package com.cnl.remotedesktop.server.audio.source;

import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.cnl.remotedesktop.config.AudioConfig;

import java.nio.ByteBuffer;

public class MICAudioSource implements AudioSource {

    private AudioRecord record;

    @Override
    public AudioConfig prepare() {
        AudioConfig config = new AudioConfig();
        int bufferSize = AudioRecord.getMinBufferSize(config.getFrequency(), config.getChannel(), config.getEncoding());
        record = new AudioRecord(MediaRecorder.AudioSource.MIC, config.getFrequency(), config.getChannel(), config.getEncoding(), bufferSize);
        return config;
    }

    @Override
    public void getAudioData(ByteBuffer bb) {
        bb.clear();
        record.read(bb, bb.limit());
    }

    @Override
    public boolean start() {
        try {
            record.startRecording();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean stop() {
        try {
            record.stop();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
