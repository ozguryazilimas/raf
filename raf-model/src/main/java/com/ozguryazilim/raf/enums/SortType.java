package com.ozguryazilim.raf.enums;

/**
 * ASC -> Eskiden yeniye
 * DESC -> Yeniden eskiye
 */
public enum SortType {
    TITLE,
    NAME,
    MODIFY_DATE_ASC,
    MODIFY_DATE_DESC,
    DATE_ASC,
    DATE_DESC,
    MIMETYPE,
    CATEGORY,
    TAG,
    SIZE;

    public static SortType defaultSortType(String sortType) {
        for (SortType s : values()) {
            if (s.name().equals(sortType)) {
                return s;
            }
        }
        return MODIFY_DATE_DESC;
    }

}