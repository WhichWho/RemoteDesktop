package com.cnl.remotedesktop.config;

import java.io.Serializable;

public class VideoConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String defFormat = "video/avc";
    private static final int defFPS = 30;
    private static final int defIFrame = 30;
    private static final int defBitrate = 1 * 1024 * 1024;

    private String format = defFormat;
    private int fps = defFPS;
    private int iFrame = defIFrame;
    private int bitrate = defBitrate;

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public int getiFrame() {
        return iFrame;
    }

    public void setiFrame(int iFrame) {
        this.iFrame = iFrame;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }
}
