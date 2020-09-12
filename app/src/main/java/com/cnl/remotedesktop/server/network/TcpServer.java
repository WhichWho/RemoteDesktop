package com.cnl.remotedesktop.server.network;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer implements Server {

    private ReceiveThread user;
    private Socket socket;

    public TcpServer(int port) throws IOException {
        ServerSocket service = new ServerSocket(port);
        socket = service.accept();
        user = new ReceiveThread(socket);
    }

    @Override
    public boolean isReady() {
        return user != null;
    }

    @Override
    public void writeInt(int size) throws IOException {
        user.writeInt(size);
    }

    @Override
    public void write(byte[] mFrameByte, int i, int size) throws IOException {
        user.write(mFrameByte, i, size);
    }

    @Override
    public void close() throws IOException {
        user.close();
    }

    @Override
    public String getAddress() {
        return socket.getInetAddress().getHostAddress();
    }

    private class ReceiveThread{

        private DataOutputStream dop;

        ReceiveThread(Socket socket) throws IOException {
            dop = new DataOutputStream(socket.getOutputStream());
        }

        private void writeInt(int size) throws IOException {
            dop.writeInt(size);
        }

        private void write(byte[] mFrameByte, int i, int size) throws IOException {
            dop.write(mFrameByte, i, size);
        }

        private void close() throws IOException {
            dop.close();
        }

    }
}
