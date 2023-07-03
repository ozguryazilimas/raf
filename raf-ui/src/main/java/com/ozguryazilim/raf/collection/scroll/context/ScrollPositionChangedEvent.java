package com.ozguryazilim.raf.collection.scroll.context;

import com.ozguryazilim.raf.collection.scroll.ScrollPosition;

public class ScrollPositionChangedEvent {
    private ScrollPosition scrollPosition;

    public ScrollPositionChangedEvent() {
    }

    public ScrollPositionChangedEvent(ScrollPosition scrollPosition) {
        this.scrollPosition = scrollPosition;
    }

    public ScrollPosition getScrollPosition() {
        return scrollPosition;
    }
}
