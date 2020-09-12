package com.cnl.remotedesktop.utils;

import android.text.TextUtils;

import java.util.Locale;

/**
 * Created by Stardust on 2017/4/24.
 */

public abstract class AbstractShell {

    public static class Result {
        public int code = -1;
        public String error;
        public String result;

        @Override
        public String toString() {
            return "ShellResult{" +
                    "code=" + code +
                    ", error='" + error + '\'' +
                    ", result='" + result + '\'' +
                    '}';
        }
    }

    protected static final String COMMAND_SU = "su";
    protected static final String COMMAND_SH = "sh";
    protected static final String COMMAND_EXIT = "exit\n";
    protected static final String COMMAND_LINE_END = "\n";

    private int mTouchDevice = -1;
    private boolean mRoot;

    public AbstractShell() {
        this(false);
    }

    public AbstractShell(boolean root) {
        mRoot = root;
        init(root ? COMMAND_SU : COMMAND_SH);
    }

    public boolean isRoot() {
        return mRoot;
    }

    protected abstract void init(String initialCommand);

    public abstract void exec(String command);

    public abstract void exit();

    public void SetTouchDevice(int touchDevice) {
        if (mTouchDevice > 0)
            return;
        mTouchDevice = touchDevice;
    }

    public void SendEvent(int type, int code, int value) {
        SendEvent(mTouchDevice, type, code, value);
    }

    public void SendEvent(int device, int type, int code, int value) {
        exec(TextUtils.join("", new Object[]{"sendevent /dev/input/event", device, " ", type, " ", code, " ", value}));
    }

    public void Touch(int x, int y) {
        TouchX(x);
        TouchY(y);
    }

    public void TouchX(int x) {
        SendEvent(mTouchDevice, 3, 53, x);
    }

    public void TouchY(int y) {
        SendEvent(mTouchDevice, 3, 54, y);
    }

    public void Tap(int x, int y) {
        exec("input tap " + x + " " + y);
    }

    public void Swipe(int x1, int y1, int x2, int y2) {
        exec(String.format(Locale.ENGLISH,"input swipe %d %d %d %d", x1, y1, x2, y2));
    }

    public void Swipe(int x1, int y1, int x2, int y2, int time) {
        exec(String.format(Locale.ENGLISH,"input swipe %d %d %d %d %d", x1, y1, x2, y2, time));
    }

    public void KeyCode(int keyCode) {
        exec("input keyevent " + keyCode);
    }

    public void KeyCode(String keyCode) {
        exec("input keyevent " + keyCode);
    }

    public void Home() {
        KeyCode(3);
    }

    public void Back() {
        KeyCode(4);
    }

    public void Power() {
        KeyCode(26);
    }

    public void Up() {
        KeyCode(19);
    }

    public void Down() {
        KeyCode(20);
    }

    public void Left() {
        KeyCode(21);
    }

    public void Right() {
        KeyCode(22);
    }

    public void OK() {
        KeyCode(23);
    }

    public void VolumeUp() {
        KeyCode(24);
    }

    public void VolumeDown() {
        KeyCode(25);
    }

    public void Menu() {
        KeyCode(1);
    }

    public void Camera() {
        KeyCode(27);
    }

    public void Input(String text) {
        exec("input text " + text);
    }

    public void ScreenCap(String path) {
        exec("screencap -p " + path);
    }

    public void Text(String text) {
        Input(text);
    }

    public void sleep(long i) {
        exec("sleep " + i);
    }

    public void usleep(long l) {
        exec("usleep " + l);
    }
}
