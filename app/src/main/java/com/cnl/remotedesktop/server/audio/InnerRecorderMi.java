package com.cnl.remotedesktop.server.audio;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.IBinder;
import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

public class InnerRecorderMi {

    private MemoryFile mData;
    private Object mMiuiAudioRecord;



    class WThread extends Thread{
        @Override
        public void run(){
            File f = new File("/sdcard/loopback.pcm");
            FileOutputStream fos = null;
            try{
                fos = new FileOutputStream(f);
            }catch(FileNotFoundException e){
                e.printStackTrace();
            }
            prepareInnerAudioEncoder();
            startInnerAudioRecording();
            ByteBuffer bb = ByteBuffer.allocate(10240);
            long t = System.currentTimeMillis();
            long max = 1000 * 60;
            try{
                while(System.currentTimeMillis() - t < max){
                    transferInnerAudioDataToEncoder(bb);
                    int len = bb.position();
                    fos.write(bb.array(), 0, len);
                }
                fos.flush();
                fos.close();
            }catch(IOException e){
                e.printStackTrace();
            }
            try{
                mMiuiAudioRecord.getClass().getDeclaredMethod("stop", new Class[0]).invoke(mMiuiAudioRecord, new Object[0]);
            }catch(Exception e3){
                Log.e("StableScreenRecorderCore", "Error when stopping mMiuiAudioRecord", e3);
            }

        }
    }

    @SuppressLint("PrivateApi")
    private void prepareInnerAudioEncoder() {
        try {
            mData = new MemoryFile("screenRecord", 10240);
            Constructor<ParcelFileDescriptor> constructor = ParcelFileDescriptor.class.getConstructor(FileDescriptor.class);
            Object[] objArr = {(FileDescriptor) mData.getClass().getDeclaredMethod("getFileDescriptor", new Class[0]).invoke(mData, new Object[0])};
            Class<?> smClazz = Class.forName("android.os.ServiceManager");
            Class<?> asStubClazz = Class.forName("android.media.IAudioService$Stub");
            Object as = asStubClazz.getDeclaredMethod("asInterface", new Class[]{IBinder.class}).invoke(asStubClazz, (IBinder) smClazz.getDeclaredMethod("getService", new Class[]{String.class}).invoke(smClazz.newInstance(), new Object[]{"audio"}));
            Method createAudioRecordForLoopback = as.getClass().getDeclaredMethod("createAudioRecordForLoopback", ParcelFileDescriptor.class, Long.TYPE);
            Object[] objArr2 = {constructor.newInstance(objArr), 10240L};
            Class<?> arClazz = Class.forName("android.media.IMiuiAudioRecord$Stub");
            mMiuiAudioRecord = arClazz.getDeclaredMethod("asInterface", new Class[]{IBinder.class}).invoke(arClazz, (IBinder) createAudioRecordForLoopback.invoke(as, objArr2));
            Bundle bundle = (Bundle) mMiuiAudioRecord.getClass().getDeclaredMethod("getMetaData", new Class[0]).invoke(this.mMiuiAudioRecord, new Object[0]);
            if (bundle != null) {
                int sampleRate = bundle.getInt("sample-rate");
                int channelCount = bundle.getInt("channel-count");
            }
        } catch (Exception e) {
            Log.e("StableScreenRecorderCore", "Exception occur about MiuiAudiorecord :" + e);
        }
    }

    private void prepareAudioEncoder(final int sampleRate, final int channelCount) {
//        MediaFormat format = MediaFormat.createAudioFormat("audio/mp4a-latm", sampleRate, channelCount);
//        format.setInteger("aac-profile", 2);
//        format.setInteger("channel-mask", 16);
//        format.setInteger("bitrate", 320000);
//        if (Build.VERSION.SDK_INT > 21) {
//            Log.v("StableScreenRecorderCore", "set KEY_PRIORITY for audio format");
//            format.setInteger("priority", 0);
//        }
//        Log.v("StableScreenRecorderCore", "create audio format: " + format);
//        try {
//            this.mAudioEncoder = MediaCodec.createEncoderByType("audio/mp4a-latm");
//            this.mAudioEncoder.configure(format, (Surface) null, (MediaCrypto) null, 1);
//            this.mAudioEncoder.start();
//            Log.v("StableScreenRecorderCore", "audio encoder start success");
//        } catch (IOException e) {
//            Log.e("StableScreenRecorderCore", "unable to create audio encoder, " + e);
//        }
    }

    private boolean startInnerAudioRecording() {
        try {
            Class<?> audioRecordClass = this.mMiuiAudioRecord.getClass();
            long startTime = System.nanoTime() / 1000;
            return (boolean) audioRecordClass.getDeclaredMethod("start", new Class[]{Long.TYPE}).invoke(this.mMiuiAudioRecord, new Object[]{startTime});
        } catch (Exception e) {
            Log.e("StableScreenRecorderCore", "mMiuiAudioRecord start fail in start()", e);
            return false;
        }
    }

    private void transferInnerAudioDataToEncoder(ByteBuffer inputBuffer) {
//        int inputBufferIndex = this.mAudioEncoder.dequeueInputBuffer(10000);
//        if (inputBufferIndex >= 0) {
//            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
        byte[] input;//= new byte[inputBuffer.capacity()];
        long pts = 0;
        try {
            Bundle info = (Bundle) this.mMiuiAudioRecord.getClass().getDeclaredMethod("fillBuffer", new Class[]{Integer.TYPE, Integer.TYPE}).invoke(this.mMiuiAudioRecord, new Object[]{0, Integer.valueOf(inputBuffer.capacity())});
            if (info != null) {
                int memorySize = (int) info.getLong("size");
                pts = info.getLong("presentationTimeUs");
                input = new byte[memorySize];
                int inputReadResult = this.mData.readBytes(input, 0, 0, memorySize);
                inputBuffer.clear();
                inputBuffer.put(input, 0, inputReadResult);
            }
//                this.mAudioEncoder.queueInputBuffer(inputBufferIndex, 0, inputBuffer.position(), pts, 0);
        } catch (Exception e) {
            Log.e("StableScreenRecorderCore", "MiuiAudioRecord read data failed,return", e);
        }
//        }
    }

}
