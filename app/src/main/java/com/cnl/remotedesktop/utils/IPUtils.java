package com.cnl.remotedesktop.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class IPUtils {

    public static String getIp(Context ctx) {
        WifiManager wm = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        if (!wm.isWifiEnabled()) return null;
        WifiInfo wi = wm.getConnectionInfo();
        int ipAdd = wi.getIpAddress();
        return intToIp(ipAdd);
    }

    private static String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }
}
