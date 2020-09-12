package com.cnl.remotedesktop.server.perform;


import android.graphics.Path;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.cnl.remotedesktop.utils.EventBean;

import java.security.Guard;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class EventPackager {

    private HashMap<Integer, Gesture> gestures;

    public EventPackager() {
        gestures = new HashMap<>();
    }

    public Gesture[] pack(EventBean bean) {
        add(bean);
        if (bean.getAct() == MotionEvent.ACTION_UP) {
            Gesture[] data = new Gesture[gestures.size()];
            Set<Integer> set = gestures.keySet();
            int j = 0;
            for(Integer i: set){
                data[j++] = gestures.get(i);
            }
            gestures.clear();
            return data;
        }
        return null;
    }

    private void add(EventBean bean) {
        Gesture gesture = gestures.get(bean.getId());
        if (gesture == null) {
            gesture = new Gesture();
            Path path = new Path();
            path.moveTo(bean.getX(), bean.getY());
            gesture.setStart(bean.getT());
            gesture.setPath(path);
            gestures.put(bean.getId(), gesture);
        } else {
            Path path = gesture.getPath();
            path.lineTo(bean.getX(), bean.getY());
            gesture.setEnd(bean.getT());
        }
    }

}
