package com.cnl.remotedesktop;

import android.app.Application;

import com.cnl.remotedesktop.utils.ToastUtils;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        ToastUtils.setContext(this);
    }
}
