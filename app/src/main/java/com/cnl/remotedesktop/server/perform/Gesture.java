package com.cnl.remotedesktop.server.perform;

import android.graphics.Path;

public class Gesture {

    private Path path;
    private long start;
    private long end;

    public Gesture() {
    }

    public Gesture(Path path, long start, long end) {
        this.path = path;
        this.start = start;
        this.end = end;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public Path getPath() {
        return path;
    }

    public int getDuration() {
        return (int) (end - start + 1);
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "Gesture{" +
                "path=" + path +
                ", start=" + start +
                ", end=" + end +
                ", duration=" + (end - start + 1) +
                '}';
    }
}
