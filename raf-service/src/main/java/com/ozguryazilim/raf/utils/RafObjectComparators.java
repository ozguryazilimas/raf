package com.ozguryazilim.raf.utils;
import com.ozguryazilim.raf.enums.SortType;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafObject;

import java.text.Collator;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RafObjectComparators {
    private static Map<SortType, Comparator<RafObject>> rafObjectComparatorMap;

    public static Comparator<RafObject> getSortTypeComparator(SortType sortType) {
        return rafObjectComparatorMap.get(sortType);
    }

    public static Comparator<RafObject> nameComparator = (o1, o2) -> compareString(o1.getName(), o2.getName());

    public static Comparator<RafObject> titleComparator = (o1, o2) -> compareString(o1.getTitle(), o2.getTitle());

    public static Comparator<RafObject> mimetypeComparator = (o1, o2) -> compareString(o1.getMimeType(), o2.getMimeType());

    public static Comparator<RafObject> categoryComparator = (o1, o2) -> compareString(o1.getCategory(), o2.getCategory());

    public static Comparator<RafObject> createdDateComparator = (o1, o2) -> compareDate(o1.getCreateDate(), o2.getCreateDate());

    public static Comparator<RafObject> updateDateComparator = (o1, o2) -> compareDate(o1.getUpdateDate(), o2.getUpdateDate());

    public static Comparator<RafObject> sizeComparator = (o1, o2) -> {
        if (o1 == null || o2 == null) return 0;
        return o1.getLength().compareTo(o2.getLength());
    };

    public static Comparator<RafObject> folderComparator = (t1, t2) -> {
        boolean f1 = (t1 instanceof RafFolder);
        boolean f2 = (t2 instanceof RafFolder);

        if (f1 && f2) {
            return 0;
        } else if (f1) {
            return -1;
        } else if (f2) {
            return 1;
        } else {
            return 0;
        }
    };

    private static int compareDate(Date d1, Date d2) {
        if (d1 == null || d2 == null) return 0;
        return d1.compareTo(d2);
    }

    private static int compareString(String s1, String s2) {
        if (s1 == null || s2 == null) return 0;
        Collator collator = Collator.getInstance(new Locale("tr", "TR"));
        return collator.compare(s1, s2);
    }

    static {
        //Initialize comparator map
        rafObjectComparatorMap = new HashMap<>();

        rafObjectComparatorMap.put(SortType.TITLE, titleComparator);
        rafObjectComparatorMap.put(SortType.NAME, nameComparator);
        rafObjectComparatorMap.put(SortType.MODIFY_DATE_ASC, updateDateComparator);
        rafObjectComparatorMap.put(SortType.MODIFY_DATE_DESC, updateDateComparator.reversed());
        rafObjectComparatorMap.put(SortType.DATE_ASC, createdDateComparator);
        rafObjectComparatorMap.put(SortType.DATE_DESC, createdDateComparator.reversed());
        rafObjectComparatorMap.put(SortType.MIMETYPE, mimetypeComparator);
        rafObjectComparatorMap.put(SortType.CATEGORY, categoryComparator);
        rafObjectComparatorMap.put(SortType.TAG, (o1, o2) -> 0);
        rafObjectComparatorMap.put(SortType.SIZE, sizeComparator);
    }
}
