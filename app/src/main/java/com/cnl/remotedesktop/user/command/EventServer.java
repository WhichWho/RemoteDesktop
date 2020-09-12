package com.cnl.remotedesktop.user.command;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.cnl.remotedesktop.MainActivity;
import com.cnl.remotedesktop.utils.EventBean;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class EventServer extends Handler {

    private String address;

    public EventServer(String address, Looper looper) {
        super(looper);
        this.address = address;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        try {
            switch (msg.what) {
                case 0:
                    init(address, MainActivity.PORT_COMMAND);
                    break;
                case 1:
                    sync((EventBean) msg.obj);
                    break;
                case 2:
                    finish();
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ObjectOutputStream oos;

    private void init(String address, int port) throws IOException {
        Socket socket = new Socket(address, port);
        oos = new ObjectOutputStream(socket.getOutputStream());
    }

    private void sync(EventBean what) throws IOException {
        oos.writeObject(what);
    }

    private void finish() throws IOException {
        oos.close();
    }


}
