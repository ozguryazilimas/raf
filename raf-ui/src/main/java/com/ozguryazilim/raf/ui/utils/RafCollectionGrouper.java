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

        switch (sortType) {
            case NAME:
                return groupingByTitle(collection);
            case MIMETYPE:
                return groupingByMimeType(collection);
            case CATEGORY:
                return groupingByCategory(collection);
            case DATE_ASC:
            case DATE_DESC:
                return groupingByCreateDate(collection);
            case MODIFY_DATE_ASC:
            case MODIFY_DATE_DESC:
                return groupingByUpdateDate(collection);
            default:
                return groupingByName(collection);
        }
    }

    private static LinkedHashMap<String, List<RafObject>> groupingByTitle(RafCollection collection) {
        return collection.getItems().stream().collect(
                Collectors.groupingBy(g -> g.getTitle().substring(0, 1).toUpperCase(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private static LinkedHashMap<String, List<RafObject>> groupingByName(RafCollection collection) {
        return collection.getItems().stream().collect(
                Collectors.groupingBy(g -> g.getName().substring(0, 1).toUpperCase(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private static LinkedHashMap<String, List<RafObject>> groupingByMimeType(RafCollection collection) {
        return collection.getItems().stream().collect(
                Collectors.groupingBy(RafObject::getMimeType,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private static LinkedHashMap<String, List<RafObject>> groupingByCategory(RafCollection collection) {
        return collection.getItems().stream().collect(
                Collectors.groupingBy(g -> g.getCategory() != null ? g.getCategory() : "",
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private static LinkedHashMap<String, List<RafObject>> groupingByCreateDate(RafCollection collection) {
        return collection.getItems().stream().collect(
                Collectors.groupingBy(g -> SIMPLE_DATE_FORMAT.format(g.getCreateDate()),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private static LinkedHashMap<String, List<RafObject>> groupingByUpdateDate(RafCollection collection) {
        return collection.getItems().stream().collect(
                Collectors.groupingBy(g -> g.getUpdateDate() != null
                                ? SIMPLE_DATE_FORMAT.format(g.getUpdateDate())
                                : SIMPLE_DATE_FORMAT.format(g.getCreateDate()),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

}