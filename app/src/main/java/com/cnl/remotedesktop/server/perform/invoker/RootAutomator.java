package com.cnl.remotedesktop.server.perform.invoker;

import android.os.SystemClock;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.cnl.remotedesktop.utils.EventBean;
import com.cnl.remotedesktop.utils.Shell;

import java.util.concurrent.atomic.AtomicInteger;

import static com.cnl.remotedesktop.server.perform.invoker.InputEventCodes.ABS_MT_POSITION_X;
import static com.cnl.remotedesktop.server.perform.invoker.InputEventCodes.ABS_MT_POSITION_Y;
import static com.cnl.remotedesktop.server.perform.invoker.InputEventCodes.ABS_MT_SLOT;
import static com.cnl.remotedesktop.server.perform.invoker.InputEventCodes.ABS_MT_TOUCH_MAJOR;
import static com.cnl.remotedesktop.server.perform.invoker.InputEventCodes.ABS_MT_TRACKING_ID;
import static com.cnl.remotedesktop.server.perform.invoker.InputEventCodes.ABS_MT_WIDTH_MAJOR;
import static com.cnl.remotedesktop.server.perform.invoker.InputEventCodes.BTN_TOUCH;
import static com.cnl.remotedesktop.server.perform.invoker.InputEventCodes.DOWN;
import static com.cnl.remotedesktop.server.perform.invoker.InputEventCodes.EV_ABS;
import static com.cnl.remotedesktop.server.perform.invoker.InputEventCodes.EV_KEY;
import static com.cnl.remotedesktop.server.perform.invoker.InputEventCodes.EV_SYN;
import static com.cnl.remotedesktop.server.perform.invoker.InputEventCodes.SYN_MT_REPORT;
import static com.cnl.remotedesktop.server.perform.invoker.InputEventCodes.SYN_REPORT;
import static com.cnl.remotedesktop.server.perform.invoker.InputEventCodes.UP;

public class RootAutomator implements PerformInvoker {

    private static final String LOG_TAG = "RootAutomator";

    public static final byte DATA_TYPE_SLEEP = 0;
    public static final byte DATA_TYPE_EVENT = 1;
    public static final byte DATA_TYPE_EVENT_SYNC_REPORT = 2;
    public static final byte DATA_TYPE_EVENT_TOUCH_X = 3;
    public static final byte DATA_TYPE_EVENT_TOUCH_Y = 4;

    private Shell mShell;
    private int mDefaultId = 0;
    private AtomicInteger mTracingId = new AtomicInteger(1);
    private SparseIntArray mSlotIdMap = new SparseIntArray();

    public RootAutomator(String exePath, String device) {
        if (device == null || "".equals(device)) device = "/dev/input/event1";
        mShell = new Shell(true);
        mShell.exec(exePath + " -d " + device);
    }

    @Override
    public void onInput(EventBean event) {
        switch (event.getAct()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                touchDown(event.getX(), event.getY(), event.getId());
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(event.getX(), event.getY(), event.getId());
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_POINTER_UP:
                touchUp(event.getId());
                break;
        }
    }

    public void sendEvent(int type, int code, int value) {
        sendEventInternal(type, code, value);
    }

    private void sendEventInternal(int type, int code, int value) {
        mShell.exec(type + " " + code + " " + value);
    }

    public void touch(int x, int y) {
        touchX(x);
        touchY(y);
    }

    public void touchX(int x) {
        sendEvent(3, 53, x);
    }

    public void touchY(int y) {
        sendEvent(3, 54, y);
    }

    public void sendSync() {
        sendEvent(EV_SYN, SYN_REPORT, 0);
    }

    public void sendMtSync() {
        sendEvent(EV_SYN, SYN_MT_REPORT, 0);
    }

    public void tap(int x, int y, int id) {
        touchDown(x, y, id);
        touchUp(id);
    }

    public void tap(int x, int y) {
        sendEvent(x, y, mDefaultId);
    }

    public void swipe(int x1, int y1, int x2, int y2, int duration, int id) {
        long now = SystemClock.uptimeMillis();
        touchDown(x1, y1, id);
        long startTime = now;
        long endTime = startTime + duration;
        while (now < endTime) {
            long elapsedTime = now - startTime;
            float alpha = (float) elapsedTime / duration;
            touchMove((int) lerp(x1, x2, alpha), (int) lerp(y1, y2, alpha), id);
            now = SystemClock.uptimeMillis();
        }
        touchUp(id);
    }

    public void swipe(int x1, int y1, int x2, int y2, int duration) {
        swipe(x1, y1, x2, y2, duration, mDefaultId);
    }

    public void swipe(int x1, int y1, int x2, int y2) {
        swipe(x1, y1, x2, y2, 500, mDefaultId);
    }

    public void press(int x, int y, int duration, int id) {
        touchDown(x, y, id);
        sleep(duration);
        touchUp(id);
    }

    public void press(int x, int y, int duration) {
        press(x, y, duration, getDefaultId());
    }

    public void longPress(int x, int y, int id) {
        press(x, y, ViewConfiguration.getLongPressTimeout() + 200, id);
    }

    public void longPress(int x, int y) {
        press(x, y, ViewConfiguration.getLongPressTimeout() + 200, getDefaultId());
    }

    public void touchDown(int x, int y, int id) {
        if (mSlotIdMap.size() == 0) {
            touchDown0(x, y, id);
            return;
        }
        int slotId = mSlotIdMap.size();
        mSlotIdMap.put(id, slotId);
        sendEvent(EV_ABS, ABS_MT_SLOT, slotId);
        sendEvent(EV_ABS, ABS_MT_TRACKING_ID, mTracingId.getAndIncrement());
        sendEvent(EV_ABS, ABS_MT_POSITION_X, x);
        sendEvent(EV_ABS, ABS_MT_POSITION_Y, y);
        sendEvent(EV_ABS, ABS_MT_TOUCH_MAJOR, 5);
        sendEvent(EV_ABS, ABS_MT_WIDTH_MAJOR, 5);
        sendEvent(EV_SYN, SYN_REPORT, 0);
    }

    private void touchDown0(int x, int y, int id) {
        mSlotIdMap.put(id, 0);
        sendEvent(EV_ABS, ABS_MT_TRACKING_ID, mTracingId.getAndIncrement());
        sendEvent(EV_KEY, BTN_TOUCH, DOWN);
        //sendEvent(EV_KEY, BTN_TOOL_FINGER, 0x00000001);
        sendEvent(EV_ABS, ABS_MT_POSITION_X, x);
        sendEvent(EV_ABS, ABS_MT_POSITION_Y, y);
        //sendEvent(EV_ABS, ABS_MT_PRESSURE, 200);
        sendEvent(EV_ABS, ABS_MT_TOUCH_MAJOR, 5);
        sendEvent(EV_ABS, ABS_MT_WIDTH_MAJOR, 5);
        sendEvent(EV_SYN, SYN_REPORT, 0);
    }

    public void touchDown(int x, int y) {
        touchDown(x, y, mDefaultId);
    }

    public void touchUp(int id) {
        int slotId;
        int i = mSlotIdMap.indexOfKey(id);
        if (i < 0) {
            slotId = 0;
        } else {
            slotId = mSlotIdMap.valueAt(i);
            mSlotIdMap.removeAt(i);
        }
        sendEvent(EV_ABS, ABS_MT_SLOT, slotId);
        sendEvent(EV_ABS, ABS_MT_TRACKING_ID, 0xffffffff);
        if (mSlotIdMap.size() == 0) {
            sendEvent(EV_KEY, BTN_TOUCH, UP);
            //sendEvent(EV_KEY, BTN_TOOL_FINGER, 0x00000000);
        }
        sendEvent(EV_SYN, SYN_REPORT, 0x00000000);
    }

    public void touchUp() {
        touchUp(mDefaultId);
    }

    public void touchMove(int x, int y, int id) {
        int slotId = mSlotIdMap.get(id, 0);
        sendEvent(EV_ABS, ABS_MT_SLOT, slotId);
        sendEvent(EV_ABS, ABS_MT_TOUCH_MAJOR, 5);
        sendEvent(EV_ABS, ABS_MT_POSITION_X, x);
        sendEvent(EV_ABS, ABS_MT_POSITION_Y, y);
        sendEvent(EV_SYN, SYN_REPORT, 0x00000000);
    }

    public void touchMove(int x, int y) {
        touchMove(x, y, mDefaultId);
    }

    public int getDefaultId() {
        return mDefaultId;
    }

    public void setDefaultId(int defaultId) {
        mDefaultId = defaultId;
    }

    private void sleep(long duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "InterruptedException", e);
            exit();
        }
    }

    private static float lerp(float a, float b, float alpha) {
        return (b - a) * alpha + a;
    }

    public void exit() {
        sleep(1);
        sendEventInternal(0xffff, 0xffff, 0xefefefef);
        mShell.exec("exit\n"); //su
        mShell.exec("exit\n"); //shell
        mShell.exit();
    }

}
