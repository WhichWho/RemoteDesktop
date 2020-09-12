package com.cnl.remotedesktop.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class ToastUtils {

    @SuppressLint("StaticFieldLeak")
    private static Context ctx;
    private static Handler hd;

    public static void setContext(Context context){
        ctx = context;
        hd = new Handler(ctx.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                Toast.makeText(ctx, String.valueOf(msg.obj), Toast.LENGTH_SHORT).show();
            }
        };
    }

    public static void toast(Object obj){
        Log.e("ToastUtil", String.valueOf(obj));
        Message msg = Message.obtain();
        msg.obj = obj;
        hd.sendMessage(msg);
    }
}
