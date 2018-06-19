/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import org.apache.commons.fileupload.FileItem;

/**
 *
 * @author oyas
 */
public class UploadRequest {

    private String raf;
    private String rafPath;
    private String uuid;
    private String fileName;
    private Integer partIndex;
    private Integer totalParts;
    private Integer chunkSize;
    private Integer totalSize;
    private Integer partByteOffset;
    private FileItem data;

    public String getRaf() {
        return raf;
    }

    public void setRaf(String raf) {
        this.raf = raf;
    }

    public String getRafPath() {
        return rafPath;
    }

    public void setRafPath(String rafPath) {
        this.rafPath = rafPath;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getPartIndex() {
        return partIndex;
    }

    public void setPartIndex(Integer partIndex) {
        this.partIndex = partIndex;
    }

    public Integer getTotalParts() {
        return totalParts;
    }

    public void setTotalParts(Integer totalParts) {
        this.totalParts = totalParts;
    }

    public Integer getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(Integer chunkSize) {
        this.chunkSize = chunkSize;
    }

    public Integer getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Integer totalSize) {
        this.totalSize = totalSize;
    }

    public Integer getPartByteOffset() {
        return partByteOffset;
    }

    public void setPartByteOffset(Integer partByteOffset) {
        this.partByteOffset = partByteOffset;
    }

    public FileItem getData() {
        return data;
    }

    public void setData(FileItem data) {
        this.data = data;
    }

    public boolean isChunkRequest(){
        return partIndex != null;
    }
    
}
