package com.cnl.remotedesktop.user;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.cnl.remotedesktop.MainActivity;
import com.cnl.remotedesktop.R;
import com.cnl.remotedesktop.config.AudioConfig;
import com.cnl.remotedesktop.user.audio.AacDecoder;
import com.cnl.remotedesktop.user.command.EventEncoder;
import com.cnl.remotedesktop.user.network.Server;
import com.cnl.remotedesktop.user.network.TcpServer;
import com.cnl.remotedesktop.user.video.VideoDecoder;
import com.cnl.remotedesktop.utils.ToastUtils;
import com.cnl.remotedesktop.views.PlaySurfaceView;

import java.io.IOException;

public class PlayActivity extends Activity {

    private PlaySurfaceView playSurface;

    private VideoDecoder videoDecoder;
    private AacDecoder audioDecoder;
    private EventEncoder eventEncoder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBackground();
        setContentView(R.layout.activity_play);
        if (getIntent().hasExtra("address")) {
            init();
        }
    }

    private void initBackground() {
        hideNavigationBar();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void init() {
        playSurface = findViewById(R.id.surface);
        playSurface.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(final SurfaceHolder holder) {
                play(holder.getSurface());
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void play(final Surface surface) {
        final String address = getIntent().getStringExtra("address");
        playSurface.setCallback(new PlaySurfaceView.OnScaleCallBack() {
            @Override
            public void onScale(float scale) {
//                Log.e("EventEncoder", "onScale");
                if (eventEncoder == null) {
                    eventEncoder = new EventEncoder(address, scale);
                }
            }
        });
        playSurface.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (eventEncoder == null) {
                    ToastUtils.toast("远程视频连接尚未建立");
                    return false;
                }
                eventEncoder.onTouch(event);
                return true;
            }
        });

        new Thread(new Runnable() {
            public void run() {
                try {
                    Server audio = new TcpServer(address, MainActivity.PORT_AUDIO);
//                    Log.e("TAG", "audio server connect!");
                    audioDecoder = new AacDecoder(audio, new AudioConfig());
                    audioDecoder.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                try {
                    Server server = new TcpServer(address, MainActivity.PORT_VIDEO);
                    videoDecoder = new VideoDecoder(surface, server);
                    videoDecoder.setCallback(new VideoDecoder.OnSizeChangeCallback() {
                        @Override
                        public void onSizeChange(int w, int h) {
                            playSurface.resizeView(w, h, false);
                        }
                    });
                    videoDecoder.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void hideNavigationBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onDestroy() {
        try {
            videoDecoder.stop();
            audioDecoder.interrupt();
            eventEncoder.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
