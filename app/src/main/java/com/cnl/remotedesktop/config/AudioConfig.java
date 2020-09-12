package com.cnl.remotedesktop.config;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import java.io.Serializable;

import static android.media.AudioFormat.CHANNEL_IN_MONO;
import static android.media.AudioFormat.CHANNEL_IN_STEREO;
import static android.media.AudioFormat.ENCODING_PCM_16BIT;

public class AudioConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int defFrequency = 44100;
    private static final int defChannelConfig = CHANNEL_IN_STEREO;
    private static final int defAudioEncoding = ENCODING_PCM_16BIT;
    private static final int defAudioBitrate = 96000;
    private static final String defPackageFormat = MediaFormat.MIMETYPE_AUDIO_AAC;
    private static final int defProfile = MediaCodecInfo.CodecProfileLevel.AACObjectLC;
    private static final int[] freqIdx = {
            96000, 88200, 64000, 48000,
            44100, 32000, 24000, 22050,
            16000, 12000, 11025, 8000
    };

    private int frequency = defFrequency;
    private int channel = defChannelConfig;
    private int encoding = defAudioEncoding;
    private int bitrate = defAudioBitrate;
    private String format = defPackageFormat;
    private int profile = defProfile;

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public void setEncoding(int encoding) {
        this.encoding = encoding;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setProfile(int profile) {
        this.profile = profile;
    }

    public int getFrequency() {
        return frequency;
    }

    public int getChannel() {
        return channel;
    }

    public int getChannelCount() {
        if (channel == CHANNEL_IN_STEREO) return 2;
        if (channel == CHANNEL_IN_MONO) return 1;
        return 2;
    }

    public static int conventAACFrequency(int index) {
        return freqIdx[index];
    }

    public int getAACFrequencyIdx() {
        for (int i = 0; i < freqIdx.length; i++) {
            if (freqIdx[i] == frequency) {
                return i;
            }
        }
        Log.e("AudioConfig", "getAACFrequencyIdx failed! freq = " + frequency);
        return -1;
    }

    public int getEncoding() {
        return encoding;
    }

    public int getBitrate() {
        return bitrate;
    }

    public String getFormat() {
        return format;
    }

    public int getProfile() {
        return profile;
    }

}
