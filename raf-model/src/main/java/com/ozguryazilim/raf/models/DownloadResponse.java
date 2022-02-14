package com.ozguryazilim.raf.models;

import java.io.Serializable;

public class DownloadResponse implements Serializable {
    private String fileName = "";
    private byte[] bytes;

    public DownloadResponse(String fileName, byte[] bytes) {
        this.fileName = fileName;
        this.bytes = bytes;
    }

    public DownloadResponse() {}

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
