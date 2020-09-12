package com.cnl.remotedesktop.server.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;

import com.cnl.remotedesktop.config.AudioConfig;
import com.cnl.remotedesktop.utils.ToastUtils;

import java.io.FileOutputStream;

public class MicRecorder {

    // 记录是否正在进行录制
    private boolean isRecording = false;

    //录制音频参数
    private int frequency = 44100;
    private int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    public void run() {
        isRecording = true;
        new RecordTask().execute();
    }

    private void fuckSound(byte[] buffer, int read){
        int x = 0;
        for(int i = 0; i < read;){
            x = 0;
            x |= buffer[i] & 0xff;
            x |= (buffer[i + 1] & 0xff) << 8;
            x *= 10;
//			if(x > 65535){
//				x = 65535;
//			}
            buffer[i] = (byte)(x & 0xff);
            buffer[i + 1] = (byte)((x >> 8) & 0xff);
            i += 2;
//			buffer[i++] *= 10;
        }
    }

    //录音线程
    class RecordTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                //开通输出流到指定的文件
                FileOutputStream fos = new FileOutputStream("/sdcard/test.aac");
                //根据定义好的几个配置，来获取合适的缓冲大小
                int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfig, audioEncoding);
                //实例化AudioRecord
                AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfig, audioEncoding, bufferSize);

                //开始录制
                record.startRecording();

                AacEncoder aacMediaEncode = new AacEncoder(new AudioConfig());
                //定义缓冲
                byte[] buffer = new byte[bufferSize];

                long t = System.currentTimeMillis();
                ToastUtils.toast("开撸");
                //定义循环，根据isRecording的值来判断是否继续录制
                while (System.currentTimeMillis() - t < 20000) {

                    //从bufferSize中读取字节。
                    int bufferReadResult = record.read(buffer, 0, bufferSize);
//                    for (int i = 0; i < buffer.length; i++) {
////                        //音量大小,此种方法放大声音会有底噪声
////                        buffer[i] = (byte) (buffer[i] * 5);//数字决定大小
////                    }
                    fuckSound(buffer, buffer.length);
                    //获取字节流
                    if (AudioRecord.ERROR_INVALID_OPERATION != bufferReadResult) {

                        //转成AAC编码
                        byte[] ret = null;// = aacMediaEncode.offerEncoder(buffer);
                        if (ret.length > 0) {
                            fos.write(ret);
                            //byte[] out = aacDecode.offerDecoder(ret);

                            //发送数据到VLC，这个方法在视频发送那篇文章有，这里就不重复了。需要的可以去看看
                            //netSendTask.pushBuf(ret, ret.length);

                        }
                    }
                }
                //录制结束
                record.stop();
                //释放编码器
                aacMediaEncode.close();

                fos.flush();
                fos.close();
                ToastUtils.toast("结束");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }


}
