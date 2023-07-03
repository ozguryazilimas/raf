package com.ozguryazilim.raf.collection.scroll;

public class ScrollPosition {
    Long yScroll;
    Long xScroll;

    public ScrollPosition() {
    }

    public ScrollPosition(Long yScroll, Long xScroll) {
        this.yScroll = yScroll;
        this.xScroll = xScroll;
    }

    public Long getyScroll() {
        return yScroll;
    }

    public Long getxScroll() {
        return xScroll;
    }

    public void setyScroll(Long yScroll) {
        this.yScroll = yScroll;
    }

    public void setxScroll(Long xScroll) {
        this.xScroll = xScroll;
    }
}
