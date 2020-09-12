package com.cnl.remotedesktop.server.network;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class TestServer {

    public TestServer() {

    }

    public void init(int port) throws IOException {
        ServerSocket service = new ServerSocket(port);
        while (true) {
            Socket socket = service.accept();
            new ReceiveThread(socket).start();
        }
    }

    private class ReceiveThread extends Thread {

        private Socket socket;
        private OutputStream oup;

        ReceiveThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                oup = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            new Timer().scheduleAtFixedRate(new TimerTask(){
                @Override
                public void run() {
                    try {
                        oup.write(("Time:"+System.currentTimeMillis()+"\n").getBytes());
                        oup.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 0, 1000);
        }
    }
}
