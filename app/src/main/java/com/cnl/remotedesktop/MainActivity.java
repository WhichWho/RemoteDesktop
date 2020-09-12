package com.cnl.remotedesktop;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cnl.remotedesktop.config.ConfigBean;
import com.cnl.remotedesktop.server.MainService;
import com.cnl.remotedesktop.user.PlayActivity;
import com.cnl.remotedesktop.utils.IPUtils;
import com.cnl.remotedesktop.utils.PermissionChecker;
import com.cnl.remotedesktop.utils.ToastUtils;

public class MainActivity extends AppCompatActivity {

    public static final int PORT_VIDEO = 2333;
    public static final int PORT_AUDIO = 2334;
    public static final int PORT_COMMAND = 2335;
    public static final String CONFIG_BIN_KEY = "CONFIG_BIN";
    public static final String MPM_REQUEST_INTENT_KEY = "MPM_REQUEST_INTENT_KEY";
    private static final int MPM_REQUEST_CODE = 0x321;
    private static TextView tv;
    private static Handler hd;
    private EditText ed;
    private PermissionChecker permission;

    private ConfigBean bean = new ConfigBean();

    public static void log(Object obj) {
        Message msg = Message.obtain();
        msg.obj = obj;
        hd.sendMessage(msg);
    }

    static {
        System.loadLibrary("native-lib");
    }

    public native String nmsl();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permission = new PermissionChecker(this);
        if (permission.checkPermission()) {
            init();
        }
    }

    @SuppressLint("DefaultLocale")
    private void init() {
        tv = findViewById(R.id.debug);
        ed = findViewById(R.id.editer);
        ed.setText("192.168.31.249");
        hd = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                tv.append(String.valueOf(msg.obj));
                tv.append("\n");
            }
        };
        newVersion();
        permission.verifyPermission();
        log(String.format("ip: %s port: %d", IPUtils.getIp(getApplication()), PORT_VIDEO));
    }

    private void newVersion() {
        Button client = findViewById(R.id.main_start_receive);
        client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PlayActivity.class);
                intent.putExtra("address", ed.getText().toString());
                startActivity(intent);
            }
        });

        Button server = findViewById(R.id.main_start_server);
        server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioGroup group = findViewById(R.id.data_source);
                switch (group.getCheckedRadioButtonId()) {
                    case R.id.source_screen:
                        bean.video = ConfigBean.SourceVideo.SCREEN;
                        break;
                    case R.id.source_camera:
                        bean.video = ConfigBean.SourceVideo.CAMERA;
                        break;
                    case R.id.source_null_video:
                        bean.video = ConfigBean.SourceVideo.NULL;
                        break;
                }
                group = findViewById(R.id.data_source_audio);
                switch (group.getCheckedRadioButtonId()) {
                    case R.id.source_sound:
                        bean.audio = ConfigBean.SourceAudio.SYSTEM;
                        break;
                    case R.id.source_mic:
                        bean.audio = ConfigBean.SourceAudio.MIC;
                        break;
                    case R.id.source_null_audio:
                        bean.audio = ConfigBean.SourceAudio.NULL;
                        break;
                }
                group = findViewById(R.id.transfer_way);
                switch (group.getCheckedRadioButtonId()) {
                    case R.id.way_tcp:
                        bean.transfer = ConfigBean.TransferMethod.TCP;
                        break;
                    case R.id.way_udp:
                        bean.transfer = ConfigBean.TransferMethod.UDP;
                        break;
                }
                group = findViewById(R.id.accessibility_root);
                switch (group.getCheckedRadioButtonId()) {
                    case R.id.way_accessibility:
                        bean.invoke = ConfigBean.InvokeMethod.ACCESSIBILITY;
                        break;
                    case R.id.way_root:
                        bean.invoke = ConfigBean.InvokeMethod.ROOT;
                        break;
                    case R.id.way_null:
                        bean.invoke = ConfigBean.InvokeMethod.NULL;
                        break;
                }

                if (bean.video == ConfigBean.SourceVideo.SCREEN) {
                    MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
                    if (mpm == null) {
                        log("MediaProjectionManager is null! Can't catch the screen");
                        return;
                    }
                    Intent captureIntent = mpm.createScreenCaptureIntent();
                    startActivityForResult(captureIntent, MPM_REQUEST_CODE);
                } else if (bean.video == ConfigBean.SourceVideo.CAMERA) {
                    startMainService(null);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MPM_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                startMainService(data);
            } else {
                log("请授予必要录屏权限！");
            }
        }
    }

    private void startMainService(Intent extra) {
        Intent intent = new Intent(MainActivity.this, MainService.class);
        intent.putExtra(CONFIG_BIN_KEY, bean);
        intent.putExtra(MPM_REQUEST_INTENT_KEY, extra);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        log("直播开始");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permission.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            init();
        } else {
            ToastUtils.toast("权限不足");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            }, 3000);
        }
    }

}
