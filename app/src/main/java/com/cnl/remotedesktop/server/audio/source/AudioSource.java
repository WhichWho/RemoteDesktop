package com.cnl.remotedesktop.server.audio.source;

import com.cnl.remotedesktop.config.AudioConfig;

import java.nio.ByteBuffer;

public interface AudioSource {
    AudioConfig prepare();

    void getAudioData(ByteBuffer bb);

    boolean start();

    boolean stop();
}
