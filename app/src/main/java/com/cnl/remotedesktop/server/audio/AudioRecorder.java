package com.cnl.remotedesktop.server.audio;

import android.util.Log;

import com.cnl.remotedesktop.config.AudioConfig;
import com.cnl.remotedesktop.server.audio.source.AudioSource;
import com.cnl.remotedesktop.server.network.Server;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AudioRecorder extends Thread {

    private AudioSource source;
    private Server os;
    private AudioConfig config;
    private boolean stopFlag;

    public AudioRecorder(AudioSource source, Server os) {
        this.source = source;
        this.os = os;
        stopFlag = false;
    }

    @Override
    public void run() {
        ByteBuffer input = ByteBuffer.allocate(10240);
        ByteBuffer output = ByteBuffer.allocate(10240);
        try {
            config = source.prepare();
            Log.e("AudioRecorder", "run: config " + config);
            AacEncoder encoder = new AacEncoder(config);
            source.start();
            long t = System.currentTimeMillis();
            while (!stopFlag) {
                source.getAudioData(input);
                input.limit(input.position());
                input.rewind();
                encoder.encode(input, output);
//                byte[] b = new byte[output.limit()];
//                output.get(b, 0, b.length);
//                os.write(b, 0, b.length);
                os.write(output.array(), 0, output.limit());
                input.clear();
                output.clear();
            }
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            source.stop();
        }
    }

    public void release() {
        stopFlag = true;
    }

    public AudioConfig getConfig() {
        return config;
    }
}
