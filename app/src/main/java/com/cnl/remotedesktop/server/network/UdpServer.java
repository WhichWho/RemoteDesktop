package com.cnl.remotedesktop.server.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class UdpServer implements Server {

    private DatagramSocket server;
    private InetSocketAddress address;
    private int sentSizeCout;
    private int packageNum;
    private String addr;

    public UdpServer(String addr, int port) throws IOException {
        server = new DatagramSocket();
        address = new InetSocketAddress(addr, port);
        sentSizeCout = 0;
        packageNum = 0;
        this.addr = addr;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void writeInt(int size) throws IOException {
//        ByteArrayOutputStream bop = new ByteArrayOutputStream();
//        DataOutputStream dop = new DataOutputStream(bop);
//        dop.writeInt(size);
//        dop.flush();

//        if (sentSizeCout < 2) sentSizeCout++;
//        else return;

//        byte[] udata = getBytes(size);/*bop.toByteArray();*/
//        byte[] data = new byte[udata.length + 20];
//        Arrays.fill(data, (byte) 0xff);
//        System.arraycopy(udata, 0, data, 10, udata.length);

//        byte[] udata = Integer.toString(size).getBytes();
//        DatagramPacket dp = new DatagramPacket(udata, udata.length, address);
//        server.send(dp);
    }

    @Override
    public void write(byte[] mFrameByte, int i, int size) throws IOException {
        byte[] data = new byte[mFrameByte.length + 4];
        byte[] num = getBytes(packageNum++);
        System.arraycopy(num, 0, data, 0, num.length);
        System.arraycopy(mFrameByte, 0, data, 4, mFrameByte.length);
        DatagramPacket dp = new DatagramPacket(data, size, address);
        server.send(dp);
    }

    @Override
    public void close() throws IOException {
        server.close();
    }

    @Override
    public String getAddress() {
        return addr;
    }

    private byte[] getBytes(int x) {
        byte[] output = new byte[4];
        output[0] = (byte) (x >> 24);
        output[1] = (byte) (x >> 16);
        output[2] = (byte) (x >> 8);
        output[3] = (byte) x;
        return output;
    }
}
