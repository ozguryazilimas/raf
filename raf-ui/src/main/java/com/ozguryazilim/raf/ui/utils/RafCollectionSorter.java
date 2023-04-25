package com.ozguryazilim.raf.ui.utils;

import com.ozguryazilim.raf.enums.SortType;
import com.ozguryazilim.raf.models.RafCollection;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.utils.RafObjectComparators;
import org.apache.commons.collections4.comparators.ComparatorChain;

import java.util.Comparator;

public final class RafCollectionSorter {
    private static SortType defaultSortType = SortType.TITLE;

    private RafCollectionSorter() {
        throw new IllegalStateException("Utility class");
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
        ComparatorChain<RafObject> comparatorChain = new ComparatorChain<>();

        if (isFoldersFirst) {
            comparatorChain.addComparator(RafObjectComparators.folderComparator);
        }

        Comparator<RafObject> mainSortConstraint = RafObjectComparators.getSortTypeComparator(sortType);
        if (reverseSort) {
            mainSortConstraint = mainSortConstraint.reversed();
        }

        comparatorChain.addComparator(mainSortConstraint);

        if (defaultSortType != sortType) {
            comparatorChain.addComparator(RafObjectComparators.titleComparator);
        }

        //Sort
        collection.getItems().sort(comparatorChain);
    }

}