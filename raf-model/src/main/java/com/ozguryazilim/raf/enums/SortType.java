package com.ozguryazilim.raf.enums;

/**
 * ASC -> Eskiden yeniye
 * DESC -> Yeniden eskiye
 */
public enum SortType {
    NAME,
    MODIFY_DATE_ASC,
    MODIFY_DATE_DESC,
    DATE_ASC,
    DATE_DESC,
    MIMETYPE,
    CATEGORY,
    TAG;

    public static SortType defaultSortType(String sortType) {
        return SortType.valueOf(sortType);
    }

}