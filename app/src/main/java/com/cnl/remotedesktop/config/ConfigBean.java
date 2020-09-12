package com.cnl.remotedesktop.config;

import java.io.Serializable;

public class ConfigBean implements Serializable {

    private static final long serialVersionUID = 1L;

    public SourceVideo video;
    public SourceAudio audio;
    public TransferMethod transfer;
    public InvokeMethod invoke;

    public enum SourceVideo {
        SCREEN, CAMERA, NULL
    }

    public enum SourceAudio {
        MIC, SYSTEM, NULL
    }

    public enum TransferMethod {
        TCP, UDP
    }

    public enum InvokeMethod {
        ACCESSIBILITY, ROOT, NULL
    }
}
