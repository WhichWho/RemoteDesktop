package com.cnl.remotedesktop.server.perform.invoker;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Stardust on 2017/8/1.
 */

public class RootAutomatorEngine {

    private static final String TAG = "RootAutomatorEngine";
    private static final String ROOT_AUTOMATOR_EXECUTABLE_ASSET = "root_automator";

    private Context mContext;
    private String mDeviceNameOrPath;

    public RootAutomatorEngine(Context context, String deviceNameOrPath) {
        mContext = context;
        mDeviceNameOrPath = deviceNameOrPath;
    }

    public RootAutomatorEngine(Context context) {
        this(context, InputDevices.getTouchDeviceName());
    }

    public String getDeviceNameOrPath() {
        return mDeviceNameOrPath;
    }

    public String getExecutablePath() {
        File tmp = new File(mContext.getCacheDir(), "root_automator");
        if (!tmp.exists())
            copyAsset(tmp.getAbsolutePath());
        Log.d(TAG, "Readable " + tmp.setReadable(true));
        Log.d(TAG, "Writable " + tmp.setWritable(true));
        Log.d(TAG, "Executable " + tmp.setExecutable(true));
        return tmp.getAbsolutePath();
    }

    private void copyAsset(String path) {
        try {
            InputStream is = mContext.getAssets().open(RootAutomatorEngine.ROOT_AUTOMATOR_EXECUTABLE_ASSET);
            FileOutputStream fos = new FileOutputStream(path);
            byte[] b = new byte[10240];
            int len;
            while ((len = is.read(b)) != -1) {
                fos.write(b, 0, len);
            }
            is.close();
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
