package com.cnl.remotedesktop.server;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.cnl.remotedesktop.MainActivity;
import com.cnl.remotedesktop.R;
import com.cnl.remotedesktop.config.ConfigBean;
import com.cnl.remotedesktop.config.VideoConfig;
import com.cnl.remotedesktop.server.audio.AudioRecorder;
import com.cnl.remotedesktop.server.audio.source.AudioSource;
import com.cnl.remotedesktop.server.audio.source.InnerMiuiAudioSource;
import com.cnl.remotedesktop.server.audio.source.MICAudioSource;
import com.cnl.remotedesktop.server.network.Server;
import com.cnl.remotedesktop.server.network.TcpServer;
import com.cnl.remotedesktop.server.perform.EventDecoder;
import com.cnl.remotedesktop.server.perform.EventServer;
import com.cnl.remotedesktop.server.perform.invoker.InputDevices;
import com.cnl.remotedesktop.server.perform.invoker.PerformInvoker;
import com.cnl.remotedesktop.server.perform.invoker.RootAutomator;
import com.cnl.remotedesktop.server.perform.invoker.RootAutomatorEngine;
import com.cnl.remotedesktop.server.perform.invoker.ScreenAccessibilityService;
import com.cnl.remotedesktop.server.video.VideoEncoder;
import com.cnl.remotedesktop.server.video.source.CameraVideoSource;
import com.cnl.remotedesktop.server.video.source.ScreenVideoSource;
import com.cnl.remotedesktop.server.video.source.VideoSource;

import java.io.IOException;

import static com.cnl.remotedesktop.utils.SizeUtils.dpi;
import static com.cnl.remotedesktop.utils.SizeUtils.height;
import static com.cnl.remotedesktop.utils.SizeUtils.width;

public class MainService extends Service {

    public static final int FRAG_SERVICE_ID = 0x112233;
    private static final String CHANNEL_ID = "1";
    private static final CharSequence CHANNEL_NAME = "CNL_Notification";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        initForeground();
        init(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    public void init(final Intent intent) {
        if (intent == null) {
            Log.e("MainService", "Intent is null");
            return;
        }
        final ConfigBean bean = (ConfigBean) intent.getSerializableExtra(MainActivity.CONFIG_BIN_KEY);
        if (bean == null) {
            Log.e("MainService", "ConfigBean is null");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Server audio = null;
                try {
                    audio = new TcpServer(MainActivity.PORT_AUDIO);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                MainActivity.log(audio.isReady() ? "音频连接成功" : "音频连接异常");
                initAudio(bean, audio);
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Server video = null;
                try {
                    video = new TcpServer(MainActivity.PORT_VIDEO);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                MainActivity.log(video.isReady() ? "视频连接成功" : "视频连接异常");
                initVideo(intent, bean, video);

            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                EventServer event = null;
                try {
                    event = new EventServer(MainActivity.PORT_COMMAND);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                MainActivity.log(event.isReady() ? "控制连接成功" : "控制连接异常");
                initGestures(bean, event);
            }
        }).start();

    }

    private void initAudio(ConfigBean bean, Server server) {
        AudioSource source = null;
        switch (bean.audio) {
            case MIC:
                source = new MICAudioSource();
                break;
            case SYSTEM:
                source = new InnerMiuiAudioSource();
                break;
            case NULL:
//                source = new FileAudioSource("/sdcard/Android/data/com.cnl.remotedesktop/fnwav");
//                break;
                return;
        }
        new AudioRecorder(source, server).start();
        MainActivity.log("audio init");
    }

    private void initVideo(Intent intent, ConfigBean bean, Server output) {
        VideoSource source = null;
        switch (bean.video) {
            case SCREEN:
                source = new ScreenVideoSource(this, (Intent) intent.getParcelableExtra(MainActivity.MPM_REQUEST_INTENT_KEY));
                break;
            case CAMERA:
                source = new CameraVideoSource(this);
                break;
            case NULL:
                return;
        }
        new VideoEncoder(source, output, new VideoConfig()).start(width, height, dpi);
        MainActivity.log("video init");
    }

    private void initGestures(ConfigBean bean, EventServer server) {
        PerformInvoker invoker = null;
        switch (bean.invoke) {
            case ACCESSIBILITY:
                invoker = ScreenAccessibilityService.self;
                break;
            case ROOT:
                RootAutomatorEngine rae = new RootAutomatorEngine(this);
                invoker = new RootAutomator(rae.getExecutablePath(), rae.getDeviceNameOrPath());
                break;
            case NULL:
                return;
        }
        new EventDecoder(invoker, server).start();
        MainActivity.log("gestures init");
    }

    private void initForeground() {
        Notification.Builder builder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }
        Intent nfIntent = new Intent(this, MainActivity.class);
        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0))
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(getString(R.string.foreground_running_content_text))
                .setWhen(System.currentTimeMillis());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_MIN);
            notificationChannel.enableLights(true);
            notificationChannel.setShowBadge(false);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(notificationChannel);
            }
            builder.setChannelId(CHANNEL_ID);
        }
        Notification notification = builder.build();
        startForeground(FRAG_SERVICE_ID, notification);
    }
}
