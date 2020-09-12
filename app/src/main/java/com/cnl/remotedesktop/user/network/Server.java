package com.cnl.remotedesktop.user.network;

import java.io.IOException;

public interface Server {

    boolean hasRelease();

    void release() throws IOException;

    void start() throws IOException;

    byte[] read(int len) throws IOException;

    int readInt() throws IOException;

    byte[] readFrame() throws IOException;

}
