package com.cnl.remotedesktop.server.perform;

import com.cnl.remotedesktop.server.perform.invoker.PerformInvoker;
import com.cnl.remotedesktop.utils.EventBean;
import com.cnl.remotedesktop.utils.ToastUtils;

public class EventDecoder extends Thread {

    private PerformInvoker invoker;
    private EventServer server;

    public EventDecoder(PerformInvoker invoker, EventServer server) {
        this.invoker = invoker;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            server.start();
            EventBean bean;
            while (!isInterrupted()) {
                bean = server.readObject();
                invoker.onInput(bean);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.toast("EventDecoder" + e);
        }
    }
}
