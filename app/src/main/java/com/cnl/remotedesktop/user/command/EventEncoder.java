package com.cnl.remotedesktop.user.command;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.MotionEvent;

import com.cnl.remotedesktop.utils.EventBean;


public class EventEncoder {

    private float scale;
    private HandlerThread worker;
    private Handler hd;

    public EventEncoder(String address, float scale) {
        this.scale = scale;
        worker = new HandlerThread("commandWorker");
        worker.start();
        hd = new EventServer(address, worker.getLooper());
        hd.sendEmptyMessage(0);
    }

    public void onTouch(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                sync(event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP:
                int idx = event.getActionIndex();
                sent(get(event, idx));
                break;
        }
    }

    public void stop() {
        hd.sendEmptyMessage(2);
        worker.quit();
    }

    private void sync(MotionEvent event) {
        int len = event.getPointerCount();
        for (int i = 0; i < len; i++) {
            sent(get(event, i));
        }
    }

    private EventBean get(MotionEvent event, int idx) {
        EventBean bean = new EventBean();
        int x = (int) (event.getX(idx) / scale);
        int y = (int) (event.getY(idx) / scale);
        long t = System.currentTimeMillis();
        int id = event.getPointerId(idx);
        int act = event.getActionMasked();
        bean.set(Math.max(0, x), Math.max(0, y), t, id, act);
        return bean;
    }

    private void sent(EventBean bean) {
        Message msg = Message.obtain();
        msg.what = 1;
        msg.obj = bean;
        hd.sendMessage(msg);
    }

}
