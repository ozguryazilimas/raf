package com.ozguryazilim.raf.ui.utils;

import com.ozguryazilim.raf.enums.SortType;
import com.ozguryazilim.raf.models.RafCollection;
import com.ozguryazilim.raf.models.RafObject;

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public final class RafCollectionGrouper {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static RafCollection collection;

    private RafCollectionGrouper() {
    }

    /**
     * Raf koleksiyonu içerisinde bulunan tüm dosya ve klasörler için gruplama yapar.
     *
     * @param sortType   -> Sıralama tipi(Aslında bunu gruplama tipi olarakta kullanıyoruz UI'da aynı değerler)
     * @param collection -> Gruplanmak istenen koleksiyon.
     * @return
     */
    public static LinkedHashMap<String, List<RafObject>> groupBy(SortType sortType, RafCollection collection) {
        RafCollectionGrouper.collection = collection;

        switch (sortType) {
            case NAME:
                return groupingByTitle();
            case MIMETYPE:
                return groupingByMimeType();
            case CATEGORY:
                return groupingByCategory();
            case DATE_ASC:
            case DATE_DESC:
                return groupingByCreateDate();
            case MODIFY_DATE_ASC:
            case MODIFY_DATE_DESC:
                return groupingByUpdateDate();
            default:
                return groupingByName();
        }
    }

    private static LinkedHashMap<String, List<RafObject>> groupingByTitle() {
        return collection.getItems().stream().collect(
                Collectors.groupingBy(g -> g.getTitle().substring(0, 1).toUpperCase(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private static LinkedHashMap<String, List<RafObject>> groupingByName() {
        return collection.getItems().stream().collect(
                Collectors.groupingBy(g -> g.getName().substring(0, 1).toUpperCase(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private static LinkedHashMap<String, List<RafObject>> groupingByMimeType() {
        return collection.getItems().stream().collect(
                Collectors.groupingBy(RafObject::getMimeType,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private static LinkedHashMap<String, List<RafObject>> groupingByCategory() {
        return collection.getItems().stream().collect(
                Collectors.groupingBy(g -> g.getCategory() != null ? g.getCategory() : "",
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private static LinkedHashMap<String, List<RafObject>> groupingByCreateDate() {
        return collection.getItems().stream().collect(
                Collectors.groupingBy(g -> SIMPLE_DATE_FORMAT.format(g.getCreateDate()),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private static LinkedHashMap<String, List<RafObject>> groupingByUpdateDate() {
        return collection.getItems().stream().collect(
                Collectors.groupingBy(g -> g.getUpdateDate() != null
                                ? SIMPLE_DATE_FORMAT.format(g.getUpdateDate())
                                : SIMPLE_DATE_FORMAT.format(g.getCreateDate()),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

}