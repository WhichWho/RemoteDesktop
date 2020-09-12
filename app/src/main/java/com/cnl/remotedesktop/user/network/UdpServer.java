package com.cnl.remotedesktop.user.network;

import com.cnl.remotedesktop.utils.ToastUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;

public class UdpServer implements Server {

    public static final int MAX_TRY = 20;
    private static final int BUFFER_SIZE = 3 * 1024 * 1024;
    int xtime = 0;
    private DatagramSocket server;
    private DatagramPacket packet;
    private LinkedBlockingQueue<byte[]> buffer;
    private LinkedBlockingQueue<byte[]> queue;
    //    private int getIntTimes = 0;
    private boolean stopflag;

    public UdpServer(int port) throws SocketException {
        server = new DatagramSocket(port);
        packet = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
        buffer = new LinkedBlockingQueue<>();
        queue = new LinkedBlockingQueue<>();
    }

    @Override
    public boolean hasRelease() {
        return false;
    }

    @Override
    public void release() throws IOException {
        stopflag = true;
        server.close();
    }

    @Override
    public void start() throws IOException {
        stopflag = false;
        new ReceiveThread().start();
        new ProcessDataThread().start();
    }

    @Override
    public byte[] read(int len) throws IOException {
        return readFrame();
    }

    @Override
    public int readInt() throws IOException {
        return getDataInt(getData());
    }

    @Override
    public byte[] readFrame() throws IOException {
        return getData();
    }

    private byte[] getData() {
        byte[] data = null;
        try {
            data = queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return data;
    }

    private boolean isDataInt(byte[] data) {
        if (data.length != 24) return false;
        for (int i = 0; i < 10; i++) {
            if (data[i] != (byte) 0xff) return false;
        }
        for (int i = 14; i < 24; i++) {
            if (data[i] != (byte) 0xff) return false;
        }
        return true;
    }

    private int getDataInt(byte[] data) {
//        return data[0] << 24 |
//                data[1] << 16 |
//                data[2] << 8 |
//                data[3];
//        if(data.length > 6){
//            ToastUtils.toast("incorrect length" + data.length);
//            return 0;
//        }
//        return Integer.parseInt(new String(data));
        xtime++;
        if (xtime == 1) {
            return 1080;
        }
        if (xtime == 2) {
            return 1920;
        }
        return 0;
    }

    private int getNumber(byte[] data) {
        return data[0] << 24 |
                data[1] << 16 |
                data[2] << 8 |
                data[3];
    }

    private class ReceiveThread extends Thread {
        @Override
        public void run() {
            try {
                while (!stopflag) {
                    server.receive(packet);
                    byte[] data = packet.getData();
                    buffer.put(data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class ProcessDataThread extends Thread {
        @Override
        public void run() {
            try {
                int current = -1;
                while (!stopflag) {
                    byte[] rawdata = buffer.take();
                    int time = getNumber(rawdata);
                    if (time > current) {
                        current = time;
                        byte[] data = new byte[rawdata.length - 4];
                        System.arraycopy(rawdata, 4, data, 0, data.length);
                        queue.put(data);
                    } else {
                        ToastUtils.toast("skip package num: " + time);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
