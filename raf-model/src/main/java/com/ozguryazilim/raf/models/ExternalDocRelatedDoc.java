package com.ozguryazilim.raf.models;

import java.io.Serializable;

/**
 * Diğer dokuman yönetim sistemi uygulamalarından içeri alınan dokuman ile
 * ilişkili dokumanlar.
 *
 */
public class ExternalDocRelatedDoc implements Serializable {

    private String rafFilePath;

    private String rafFileId;

    public String getRafFileId() {
        return rafFileId;
    }

    public String getRafFilePath() {
        return rafFilePath;
    }

    public void setRafFileId(String rafFileId) {
        this.rafFileId = rafFileId;
    }

    public void setRafFilePath(String rafFilePath) {
        this.rafFilePath = rafFilePath;
    }

}
