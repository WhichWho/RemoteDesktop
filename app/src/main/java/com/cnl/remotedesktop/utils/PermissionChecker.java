package com.cnl.remotedesktop.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.cnl.remotedesktop.R;
import com.cnl.remotedesktop.server.perform.invoker.ScreenAccessibilityService;

public class PermissionChecker {

    private static final int REQUEST_PERMISSION_CODE = 0x123;
    private static String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };

    private Activity ctx;

    public PermissionChecker(Activity activity) {
        ctx = activity;
    }

    public void verifyPermission() {
        final Button permission_floating = ctx.findViewById(R.id.permission_floating);
        permission_floating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startFloatingWindow()) {
                    ToastUtils.toast("已有悬浮窗权限");
                }
            }
        });
        final Button permission_accessibility = ctx.findViewById(R.id.permission_accessibility);
        permission_accessibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ScreenAccessibilityService.isStarted && ScreenAccessibilityService.self != null) {
                    ToastUtils.toast("服务已启动");
                } else {
                    startAccessibilityService();
                }
            }
        });
    }

    private void startAccessibilityService() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        ctx.startActivity(intent);
    }

    private boolean startFloatingWindow() {
        final boolean able = Settings.canDrawOverlays(ctx);
        if (!able) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + ctx.getPackageName()));
            ctx.startActivityForResult(intent, 100);
        }
        return able;
    }

    public boolean checkPermission() {
        if (checkAll()) {
            return true;
        } else {
            ActivityCompat.requestPermissions(ctx, PERMISSIONS, REQUEST_PERMISSION_CODE);
            return false;
        }
    }

    private boolean checkAll() {
        for (String permission : PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(ctx, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public boolean onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        return requestCode == REQUEST_PERMISSION_CODE && checkResultAllGranted(grantResults);
    }

    private boolean checkResultAllGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}
