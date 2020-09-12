package com.cnl.remotedesktop.user.audio;

import android.media.AudioManager;
import android.media.AudioTrack;

import com.cnl.remotedesktop.config.AudioConfig;

import java.nio.ByteBuffer;

public class AudioTrackPlayer {

    private AudioTrack audioTrack;

    public void init(AudioConfig config){
        int bufferSizeInBytes = AudioTrack.getMinBufferSize(config.getFrequency(),
                config.getChannel(), config.getEncoding());
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, config.getFrequency(),
                config.getChannel(), config.getEncoding(), bufferSizeInBytes , AudioTrack.MODE_STREAM);
        audioTrack.play();
    }

    public void play(byte[] audioData, int offset, int length) {
        audioTrack.write(audioData, offset, length);
    }

    public void play(ByteBuffer audioData, int length){
        audioTrack.write(audioData, length, AudioTrack.WRITE_BLOCKING);
    }

    public void stop(){
        audioTrack.stop();
        audioTrack.release();
    }

}
