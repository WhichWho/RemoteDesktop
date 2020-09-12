package com.cnl.remotedesktop.server.perform.invoker;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;

import com.cnl.remotedesktop.server.perform.EventPackager;
import com.cnl.remotedesktop.server.perform.Gesture;
import com.cnl.remotedesktop.utils.EventBean;
import com.cnl.remotedesktop.utils.ToastUtils;


public class ScreenAccessibilityService extends AccessibilityService implements PerformInvoker {

    public static boolean isStarted;
    public static PerformInvoker self;

    private EventPackager packager;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        packager = new EventPackager();
        isStarted = true;
        self = this;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInput(EventBean bean) {
        Gesture[] gesture = packager.pack(bean);
        if(gesture != null){
            invoke(gesture);
        }
    }

    private void invoke(Gesture... gesture) {
        GestureDescription.Builder builder = new GestureDescription.Builder();
        for(Gesture g: gesture){
            builder.addStroke(new GestureDescription.StrokeDescription(g.getPath(), 0, g.getDuration()));
        }
        if (!dispatchGesture(builder.build(), null, null)) {
            ToastUtils.toast("执行远程手势失败！");
        }
    }

    @Override
    public void onInterrupt() {
        isStarted = false;
        ToastUtils.toast("辅助服务被打断");
    }

    public static boolean isAccessibilitySettingsOn(Context mContext, Class<? extends AccessibilityService> clazz) {
        int accessibilityEnabled = 0;
        final String service = mContext.getPackageName() + "/" + clazz.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
