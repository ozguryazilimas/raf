package com.ozguryazilim.raf.enums;

public enum ShareTime {

    SHARE_24(86400000L),
    SHARE_48(172800000L),
    SHARE_72(259200000L),
    SHARE_LIMITLESS(0L);

    Long value;

    ShareTime(Long value) {
        this.value = value;
    }

    public Long millisecond() {
        return value;
    }
}
