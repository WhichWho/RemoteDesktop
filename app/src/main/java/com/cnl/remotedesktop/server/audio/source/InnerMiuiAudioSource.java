package com.cnl.remotedesktop.server.audio.source;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.cnl.remotedesktop.config.AudioConfig;

import java.io.FileDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

public class InnerMiuiAudioSource implements AudioSource {

    private static final String TAG = "InnerMiuiAudioSource";

    private int memoryBufferSize = 10240;
    private byte[] memoryBuffer;
    private MemoryFile mData;
    private Object mMiuiAudioRecord;
    private Method fillBufferMethod;

    @SuppressLint({"DiscouragedPrivateApi", "PrivateApi"})
    @Override
    public AudioConfig prepare() {
        AudioConfig config = new AudioConfig();
        try {
            mData = new MemoryFile("screenRecord", memoryBufferSize);
            memoryBuffer = new byte[memoryBufferSize];

            Constructor<ParcelFileDescriptor> constructor = ParcelFileDescriptor.class.getConstructor(FileDescriptor.class);
            FileDescriptor fileDescriptor = (FileDescriptor) MemoryFile.class.getDeclaredMethod("getFileDescriptor").invoke(mData);
            Class<?> smClazz = Class.forName("android.os.ServiceManager");
            Class<?> asStubClazz = Class.forName("android.media.IAudioService$Stub");
            Class<?> arClazz = Class.forName("android.media.IMiuiAudioRecord$Stub");

            IBinder service = (IBinder) smClazz.getDeclaredMethod("getService", String.class).invoke(smClazz.newInstance(), "audio");
            Object asInterface = asStubClazz.getDeclaredMethod("asInterface", IBinder.class).invoke(asStubClazz, service);
            if (asInterface == null) throw new Exception("can't create audio interface");
            Method loopback = asInterface.getClass().getDeclaredMethod("createAudioRecordForLoopback", ParcelFileDescriptor.class, Long.TYPE);
            IBinder loopbackIB = (IBinder) loopback.invoke(asInterface, constructor.newInstance(fileDescriptor), (long) memoryBufferSize);
            mMiuiAudioRecord = arClazz.getDeclaredMethod("asInterface", IBinder.class).invoke(arClazz, loopbackIB);
            if (mMiuiAudioRecord == null) throw new Exception("MiuiAudioRecord is NULL");
            fillBufferMethod = mMiuiAudioRecord.getClass().getDeclaredMethod("fillBuffer", Integer.TYPE, Integer.TYPE);
            Bundle bundle = (Bundle) mMiuiAudioRecord.getClass().getDeclaredMethod("getMetaData").invoke(mMiuiAudioRecord);
            if (bundle != null) {
                int sampleRate = bundle.getInt("sample-rate");
                int channelCount = bundle.getInt("channel-count");
                config.setFrequency(sampleRate);
                if(channelCount == 1){
                    config.setChannel(AudioFormat.CHANNEL_IN_MONO);
                }
                Log.e(TAG, String.format("sample-rate: %d, channel-count: %d", sampleRate, channelCount));
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception occur about MiuiAudioRecord :", e);
            return null;
        }
        return config;
    }

    @Override
    public void getAudioData(ByteBuffer bb) {
        int inputReadResult = -1;
        try {
            Bundle info = (Bundle) fillBufferMethod.invoke(mMiuiAudioRecord, 0, bb.capacity());
            if (info != null) {
                int memorySize = (int) info.getLong("size");
                long pts = info.getLong("presentationTimeUs");
                inputReadResult = mData.readBytes(memoryBuffer, 0, 0, memorySize);
                bb.clear();
                bb.put(memoryBuffer, 0, inputReadResult);
            }
        } catch (Exception e) {
            Log.e(TAG, "MiuiAudioRecord read data failed,return", e);
        }
    }

    @Override
    public boolean start() {
        try {
            long startTime = System.nanoTime() / 1000;
            return (boolean) mMiuiAudioRecord.getClass().getDeclaredMethod("start", Long.TYPE).invoke(mMiuiAudioRecord, startTime);
        } catch (Exception e) {
            Log.e(TAG, "mMiuiAudioRecord start fail in start()", e);
            return false;
        }
    }

    @Override
    public boolean stop() {
        try {
            mMiuiAudioRecord.getClass().getDeclaredMethod("stop").invoke(mMiuiAudioRecord);
        } catch (Exception e) {
            Log.e(TAG, "Error when stopping mMiuiAudioRecord", e);
            return false;
        }
        return true;
    }
}
