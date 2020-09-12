package com.cnl.remotedesktop;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaCodec;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.cnl.remotedesktop.config.AudioConfig;
import com.cnl.remotedesktop.server.audio.AacEncoder;
import com.cnl.remotedesktop.server.audio.AacEncoder2;
import com.cnl.remotedesktop.server.audio.source.FileAudioSource;
import com.cnl.remotedesktop.server.audio.source.InnerMiuiAudioSource;
import com.cnl.remotedesktop.user.audio.AacDecoder;
import com.cnl.remotedesktop.user.audio.AudioTrackPlayer;
import com.cnl.remotedesktop.utils.ToastUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


public class DebugActivity extends FragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if(false){
            //内录
            new Thread(new Runnable() {
                @Override
                public void run() {
                    InnerMiuiAudioSource source = null;
                    try {
                        source = new InnerMiuiAudioSource();
                        AudioConfig config = source.prepare();
                        source.start();
                        FileOutputStream fos = new FileOutputStream("/sdcard/Android/data/com.cnl.remotedesktop/nl.aac");
                        FileChannel foc = fos.getChannel();
                        AacEncoder enc = new AacEncoder(config);
                        ByteBuffer in = ByteBuffer.allocate(10240);
                        ByteBuffer out = ByteBuffer.allocate(10240);
                        long t = System.currentTimeMillis();
                        while (true) {
                            source.getAudioData(in);

                            in.limit(in.position());
                            in.rewind();
                            enc.encode(in, out);

                            foc.write(out);
                            in.clear();
                            out.clear();
                            if (System.currentTimeMillis() - t > 15 * 1000) break;
                        }
                        enc.close();
                        fos.close();
                        foc.close();
                        Log.e("123", "66666");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (source != null) {
                            source.stop();
                        }
                    }

                }
            });// .start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        FileOutputStream fos = new FileOutputStream("/sdcard/Android/data/com.cnl.remotedesktop/fn.aac");
                        FileInputStream fis = new FileInputStream("/sdcard/Android/data/com.cnl.remotedesktop/fnwav");
                        AacEncoder2 enc = new AacEncoder2();
                        byte[] b = new byte[10240];
                        int len = -1;
                        while ((len = fis.read(b)) > 0) {
                            byte[] bb = enc.offerEncoder(b);
                            fos.write(bb);
                        }
                        fis.close();
                        fos.close();
                        enc.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });//.start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    String path = "/sdcard/Android/data/com.cnl.remotedesktop/fn2.aac";
                    com.cnl.remotedesktop.user.network.Server audio = new com.cnl.remotedesktop.user.network.LocalServer(path);
                    Log.e("TAG", "audio server connect!");
                    AacDecoder decoder = new AacDecoder(audio, new AudioConfig());
                    decoder.start();
                }
            });//.start();

            //编码
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        FileChannel fis = new FileInputStream("/sdcard/Android/data/com.cnl.remotedesktop/fnwav").getChannel();
                        FileOutputStream fos = new FileOutputStream("/sdcard/Android/data/com.cnl.remotedesktop/fnxx.aac");
                        FileChannel foc = fos.getChannel();
                        AacEncoder enc = new AacEncoder(new AudioConfig());
                        ByteBuffer in = ByteBuffer.allocate(10240);
                        ByteBuffer out = ByteBuffer.allocate(10240);
                        while (fis.read(in) > 0) {
                            in.limit(in.position());
                            in.rewind();
                            enc.encode(in, out);

                            foc.write(out);
                            in.clear();
                            out.clear();
                        }
                        enc.close();
                        fis.close();
                        fos.close();
                        foc.close();
                        Log.e("123", "66666");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });//.start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    FileAudioSource fas = new FileAudioSource("/sdcard/Android/data/com.cnl.remotedesktop/fnwav");
                    AudioTrackPlayer player = new AudioTrackPlayer();
                    player.init(new AudioConfig());
                    ByteBuffer bb = ByteBuffer.allocate(10240);
                    while (true) {
                        bb.clear();
                        fas.getAudioData(bb);
                        byte[] b = bb.array();
                        player.play(b, 0, b.length);
                    }
                }
            });//.start();
        }


//        if (true) return;
        setContentView(R.layout.activity_main2);
        ToastUtils.setContext(this);
        setStatusBarTransparent();
        splash();
        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_view);
        final ViewPager2 vp2 = findViewById(R.id.main_viewpager);
        bottomNavigationView.setBackground(new ColorDrawable(0x88000000));
        bottomNavigationView.setItemTextColor(getColorStateList(R.color.button_text));
        bottomNavigationView.setItemIconTintList(getColorStateList(R.color.button_text));
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_download:
                        vp2.setCurrentItem(0, true);
                        break;
                    case R.id.menu_upload:
                        vp2.setCurrentItem(1, true);
                        break;
                    case R.id.menu_info:
                        vp2.setCurrentItem(2, true);
                        break;
                }
                //ToastUtils.toast(item.getTitle());
                return true;
            }
        });
        vp2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        bottomNavigationView.setSelectedItemId(R.id.menu_download);
                        break;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.menu_upload);
                        break;
                    case 2:
                        bottomNavigationView.setSelectedItemId(R.id.menu_info);
                        break;
                }
            }
        });
        vp2.setAdapter(new XAdapter(getSupportFragmentManager(), getLifecycle()));

        //allSupportCameraSize();
    }

    public static class XFragment extends Fragment {

        int x;

        public XFragment(int x) {
            this.x = x;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            RelativeLayout.LayoutParams lpm = new RelativeLayout.LayoutParams(-1, -1);
            TextView v = new TextView(getContext());
            v.setText("!!! " + x);
            v.setTextSize(25);
            v.setLayoutParams(lpm);
            v.setGravity(Gravity.CENTER);
            return v;
        }
    }

    class XAdapter extends FragmentStateAdapter {

        public XAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return new XFragment(position);
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }


    private void allSupportCameraSize() {
        CameraManager manager = getSystemService(CameraManager.class);
        try {
            if (manager != null) {
                for (String cameraId : manager.getCameraIdList()) {
                    CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                    Integer tag = characteristics.get(CameraCharacteristics.LENS_FACING);
                    if (tag == null || tag == CameraCharacteristics.LENS_FACING_FRONT) continue;
                    StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    if (map == null) continue;
                    Size[] data = map.getOutputSizes(MediaCodec.class);
                    for (Size i : data) {
                        Log.e("size", i.toString());
                    }
                    break;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setStatusBarTransparent() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        window.getDecorView().setSystemUiVisibility(flags);
        window.setNavigationBarColor(Color.TRANSPARENT);
        window.setStatusBarColor(Color.TRANSPARENT);
    }

    private void splash() {
        try {
            ImageView img = (ImageView) findViewById(R.id.main_bg);
            InputStream am = getAssets().open("splash.webp");
            Bitmap bmp = BitmapFactory.decodeStream(am);
            bmp = ReduceLight(bmp, 0x88);
            bmp = RoughGlass(this, bmp, 12);
            img.setImageBitmap(bmp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap RoughGlass(Context ctx, Bitmap fbmp, int radius) {
        Bitmap bitmap = fbmp.copy(Bitmap.Config.ARGB_8888, true);
        RenderScript rs = RenderScript.create(ctx);
        Allocation input = Allocation.createFromBitmap(rs, bitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
        Allocation output = Allocation.createTyped(rs, input.getType());
        Allocation overlayAlloc = Allocation.createFromBitmap(rs, bitmap);
        ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, overlayAlloc.getElement());
        script.setRadius(radius);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(bitmap);
        return bitmap;
//        StackBlurManager blur = new StackBlurManager(fbmp);
//        return blur.process(radius);
    }

    public static Bitmap ReduceLight(Bitmap bitmap, int radius) {
        if (!bitmap.isMutable()) {
            bitmap = bitmap.copy(bitmap.getConfig(), true);
        }
        Canvas canvas = new Canvas(bitmap);
        canvas.drawARGB(radius, 0, 0, 0);
        return bitmap;
    }

}
