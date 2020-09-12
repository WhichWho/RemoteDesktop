package com.cnl.remotedesktop.utils;

import java.io.Serializable;

public class EventBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private int x;
    private int y;
    private long t;
    private int id;
    private int act;

    public EventBean() {
    }

    public EventBean(int x, int y, long t, int id, int act) {
        this.x = x;
        this.y = y;
        this.t = t;
        this.id = id;
        this.act = act;
    }

    public void set(int x, int y, long t, int id, int act) {
        this.x = x;
        this.y = y;
        this.t = t;
        this.id = id;
        this.act = act;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public long getT() {
        return t;
    }

    public int getId() {
        return id;
    }

    public int getAct() {
        return act;
    }

    @Override
    public String toString() {
        return "EventBean{" +
                "x=" + x +
                ", y=" + y +
                ", d=" + t +
                '}';
    }
}
