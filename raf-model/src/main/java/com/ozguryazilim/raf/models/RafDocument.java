package com.ozguryazilim.raf.models;

/**
 *
 * @author oyas
 */
public class RafDocument extends RafObject{

    private Boolean hasPreview = Boolean.FALSE;
    private String previewMimeType;
    
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
    
}
