package com.cnl.remotedesktop.user.network;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class TcpServer implements Server {

    private static final int MAX_TRY = 20;

    private Socket source;
    private DataInputStream dis;

    public TcpServer(String addr, int port) throws IOException {
        source = new Socket(addr, port);
    }

    @Override
    public boolean hasRelease() {
        return source.isClosed();
    }

    @Override
    public void release() throws IOException {
        source.close();
    }

    @Override
    public void start() throws IOException {
        int count = 0;
        while(!source.isConnected()){
            if(++count > MAX_TRY) throw new RuntimeException("连接失败");
        }
        dis = new DataInputStream(source.getInputStream());
    }

    @Override
    public byte[] read(int len) throws IOException {
        byte[] b = new byte[len];
        dis.readFully(b);
        return b;
    }

    @Override
    public int readInt() throws IOException {
        return dis.readInt();
    }

    @Override
    public byte[] readFrame() throws IOException {
        int len = dis.readInt();
        byte[] b = new byte[len];
        dis.readFully(b);
        return b;
    }

}
