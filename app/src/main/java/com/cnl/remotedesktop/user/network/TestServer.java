package com.cnl.remotedesktop.user.network;

import com.cnl.remotedesktop.MainActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class TestServer {

    public TestServer() {

    }

    public void init(String addr, int port) throws IOException {
        Socket socket = new Socket(addr, port);
        InputStream inp = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inp));
        String buff;
        while ((buff = reader.readLine()) != null) {
            MainActivity.log(buff);
        }
    }

}
