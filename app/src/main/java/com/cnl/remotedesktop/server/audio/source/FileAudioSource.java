package com.cnl.remotedesktop.server.audio.source;

import android.util.Log;

import com.cnl.remotedesktop.config.AudioConfig;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileAudioSource implements AudioSource {

    private FileChannel fis;

    public FileAudioSource(String path) {
        try {
            fis = new FileInputStream(path).getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public AudioConfig prepare() {
        return new AudioConfig();
    }

    @Override
    public void getAudioData(ByteBuffer bb) {
        try {
            bb.clear();
            int x = fis.read(bb);
            Log.e("TAG", "getAudioData! realRead = " + x);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        try {
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
