package com.cnl.remotedesktop.server.perform;

import com.cnl.remotedesktop.utils.EventBean;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class EventServer {

    private static final int MAX_TRY = 20;
    private Socket source;
    private ObjectInputStream ois;

    public EventServer(int port) throws IOException {
        ServerSocket service = new ServerSocket(port);
        source = service.accept();
    }

    public void start() throws IOException {
        int count = 0;
        while(!source.isConnected()){
            if(++count > MAX_TRY) throw new RuntimeException("连接失败");
        }
        ois = new ObjectInputStream(source.getInputStream());
    }

    public EventBean readObject() throws IOException, ClassNotFoundException {
        return (EventBean) ois.readObject();
    }

    public boolean isReady(){
        return source.isConnected();
    }

}
