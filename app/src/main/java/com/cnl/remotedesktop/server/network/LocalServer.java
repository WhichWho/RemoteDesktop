package com.cnl.remotedesktop.server.network;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class LocalServer implements Server {

    private DataOutputStream dop;

    public LocalServer(String path) throws FileNotFoundException {
        dop = new DataOutputStream(new FileOutputStream(path));
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void writeInt(int size) throws IOException {
        dop.writeInt(size);
    }

    @Override
    public void write(byte[] mFrameByte, int i, int size) throws IOException {
        dop.write(mFrameByte, i, size);
    }

    @Override
    public void close() throws IOException {
        dop.close();
    }

    @Override
    public String getAddress() {
        return null;
    }

}
