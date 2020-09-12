package com.cnl.remotedesktop.user.network;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class LocalServer implements Server {
    private DataInputStream source;

    public LocalServer(String path) {
        try {
            source = new DataInputStream(new FileInputStream(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasRelease() {
        return false;
    }

    @Override
    public void release() throws IOException {
        source.close();
    }

    @Override
    public void start() {

    }

    @Override
    public byte[] read(int len) throws IOException {
        byte[] b = new byte[len];
        source.readFully(b);
        return b;
    }

    @Override
    public int readInt() throws IOException {
        return source.readInt();
    }

    @Override
    public byte[] readFrame() throws IOException {
        int len = source.readInt();
        byte[] b = new byte[len];
        source.readFully(b);
        return b;
    }

}
