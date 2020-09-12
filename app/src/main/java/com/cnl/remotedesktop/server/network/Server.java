package com.cnl.remotedesktop.server.network;

import java.io.IOException;

public interface Server {

    boolean isReady();

    void writeInt(int size) throws IOException;

    void write(byte[] mFrameByte, int i, int size) throws IOException;

    void close() throws IOException;

    String getAddress();
}
