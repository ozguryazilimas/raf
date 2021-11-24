package com.ozguryazilim.raf.ui.utils;

import com.ozguryazilim.raf.enums.SortType;
import com.ozguryazilim.raf.models.RafCollection;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafObject;

import java.text.Collator;
import java.util.Date;
import java.util.Locale;

public final class RafCollectionSorter {

    private static boolean reverseSort;
    private static boolean isFolderFirst;
    private static RafCollection collection;

    private RafCollectionSorter() {
    }

    /**
     * Raf koleksiyonu içerisinde bulunan tüm dosya ve klasörler için sıralama yapar.
     *
     * @param sortType       -> Sıralama tipi
     * @param reverseSort    -> UI'da işaretli "Ters Sıralı" opsiyonu
     * @param isFoldersFirst -> UI'da işaretli "Önce Klasörler" opsiyonu(Bu işaretli ise KLASÖR'ler
     *                       her zaman sıralamanın üstünde yer alacak)
     * @param collection     -> Sıralama yapılacak olan koleksiyon
     */
    public static void sort(SortType sortType, boolean reverseSort, boolean isFoldersFirst, RafCollection collection) {

        RafCollectionSorter.reverseSort = reverseSort;
        RafCollectionSorter.isFolderFirst = isFoldersFirst;
        RafCollectionSorter.collection = collection;

        switch (sortType) {
            case NAME:
                sortByTitle();
                break;
            case MIMETYPE:
                sortByMimeType();
                break;
            case CATEGORY:
                sortByCategory();
                break;
            case DATE_ASC:
                sortByCreateDate(true);
                break;
            case DATE_DESC:
                sortByCreateDate(false);
                break;
            case MODIFY_DATE_ASC:
                sortByUpdateDate(true);
                break;
            case MODIFY_DATE_DESC:
                sortByUpdateDate(false);
                break;
            default:
                sortByName();
                break;
        }
    }

    private static void sortByTitle() {
        collection.getItems().sort((t1, t2) -> {
            if (isFolderFirst) {
                int r = compareFolder(t1, t2);
                if (r != 0) return r;
            }
            return compareString(t1.getTitle(), t2.getTitle());
        });
    }

    private static void sortByMimeType() {
        collection.getItems().sort((t1, t2) -> {
            if (isFolderFirst) {
                int r = compareFolder(t1, t2);
                if (r != 0) return r;
            }
            return compareString(t1.getMimeType(), t2.getMimeType());
        });
    }

    private static void sortByCategory() {
        collection.getItems().sort((t1, t2) -> {
            if (isFolderFirst) {
                int r = compareFolder(t1, t2);
                if (r != 0) return r;
            }
            return compareString(t1.getCategory(), t2.getCategory());
        });
    }

    private static void sortByCreateDate(boolean isASC) {
        collection.getItems().sort((t1, t2) -> {
            if (isFolderFirst) {
                int r = compareFolder(t1, t2);
                if (r != 0) return r;
            }
            if (isASC) {
                return compareDate(t1.getCreateDate(), t2.getCreateDate());
            }
            return compareDate(t2.getCreateDate(), t1.getCreateDate());
        });
    }

    private static void sortByUpdateDate(boolean isASC) {
        collection.getItems().sort((t1, t2) -> {
            if (isFolderFirst) {
                int r = compareFolder(t1, t2);
                if (r != 0) return r;
            }
            if (isASC) {
                return compareDate(t1.getUpdateDate(), t2.getUpdateDate());
            }
            return compareDate(t2.getUpdateDate(), t1.getUpdateDate());
        });
    }

    private static void sortByName() {
        collection.getItems().sort((t1, t2) -> {
            if (isFolderFirst) {
                int r = compareFolder(t1, t2);
                if (r != 0) return r;
            }
            return compareString(t1.getName(), t2.getName());
        });
    }

    private static int compareDate(Date d1, Date d2) {
        if (d1 == null || d2 == null) return 0;
        return reverseSort ? d2.compareTo(d1) : d1.compareTo(d2);
    }

    private static int compareString(String s1, String s2) {
        if (s1 == null || s2 == null) return 0;
        Collator collator = Collator.getInstance(new Locale("tr", "TR"));
        return reverseSort ? collator.compare(s2, s1) : collator.compare(s1, s2);
    }

    private static int compareFolder(RafObject t1, RafObject t2) {
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
    }

}