/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.models;

/**
 *
 * @author oyas
 */
public class RafDocument extends RafObject{

    private Boolean hasPreview = Boolean.FALSE;
    private String previewMimeType;
    private Boolean signed = Boolean.FALSE;
    private RafSignature signature;

    public Boolean getHasPreview() {
        return hasPreview;
    }

    public void setHasPreview(Boolean hasPreview) {
        this.hasPreview = hasPreview;
    }

    public String getPreviewMimeType() {
        return previewMimeType;
    }

    public void setPreviewMimeType(String previewMimeType) {
        this.previewMimeType = previewMimeType;
    }

    public Boolean getSigned() {
        return signed;
    }

    public void setSigned(Boolean signed) {
        this.signed = signed;
    }

    public RafSignature getSignature() {
        return signature;
    }

    public void setSignature(RafSignature signature) {
        this.signature = signature;
    }
    
}
