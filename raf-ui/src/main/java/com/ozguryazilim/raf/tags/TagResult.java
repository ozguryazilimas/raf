package com.ozguryazilim.raf.tags;

public class TagResult{
    private String tag;
    private boolean absent;

    public TagResult(String tag) {
        this.tag = tag;
    }

    public TagResult(String tag, boolean absent) {
        this.tag = tag;
        this.absent = absent;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public boolean isAbsent() {
        return absent;
    }

    public void setAbsent(boolean absent) {
        this.absent = absent;
    }
}
