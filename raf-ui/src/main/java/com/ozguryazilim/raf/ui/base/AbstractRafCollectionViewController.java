package com.ozguryazilim.raf.ui.base;

import com.google.common.base.Strings;
import com.ozguryazilim.mutfak.kahve.Kahve;
import com.ozguryazilim.mutfak.kahve.annotations.UserAware;
import com.ozguryazilim.raf.IconResolver;
import com.ozguryazilim.raf.RafController;
import com.ozguryazilim.raf.events.RafFolderChangeEvent;
import com.ozguryazilim.raf.models.RafCollection;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.telve.utils.DateUtils;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * RafCollection için temel view controller sınıfı.
 *
 * Tek görevi Aldığı sort ve group bilgilerine göre nesneleri sıralamak ve
 * gruplamak.
 *
 * @author Hakan Uygun
 */
public abstract class AbstractRafCollectionViewController implements RafCollectionViewController {

    private static final String SORT_BY_NAME = "NAME";
    private static final String SORT_BY_DATE = "DATE";
    private static final String SORT_BY_MIMETYPE = "MIMETYPE";
    private static final String SORT_BY_CATEGORY = "CATEGORY";
    private static final String SORT_BY_TAG = "TAG";

    @Inject
    private IconResolver iconResolver;

    @Inject
    private RafController rafController;

    @Inject
    private Event<RafFolderChangeEvent> folderChangeEvent;

    @Inject
    @UserAware
    private Kahve kahve;

    private RafCollection collection;

    private Boolean foldersFirst = Boolean.TRUE;
    private Boolean groupBy = Boolean.FALSE;
    private Boolean showDetails = Boolean.FALSE;

    private List<String> groupNames = new ArrayList<>();
    private Map<String, List<RafObject>> groupMap = new HashMap<>();

    @Override
    public String getIcon() {
        if (getCollection() != null && getCollection().getMimeType() != null) {
            if ("raf/folder".equals(getCollection().getMimeType())) {
                return "fa-folder-open";
            }
            return iconResolver.getIcon(getCollection().getMimeType());
        } else {
            return "fa fa-file";
        }
    }

    @Override
    public String getTitle() {
        return getCollection() != null ? getCollection().getTitle() : "";
    }

    @Override
    public RafCollection getCollection() {
        return collection;
    }

    @Override
    public void setCollection(RafCollection collection) {
        this.collection = collection;
    }

    public List<String> getGroupNames() {
        if (groupNames.isEmpty()) {
            //FIXME: Burada arayüzden alınan sıralama ve gruplama yetenekleri kullanılacak
            SimpleDateFormat sdfForSort = new SimpleDateFormat("yyyy-MM-dd");
            //Eğer Gruplama gösterilmeyecek ise sadece tek bir grup olacak ve tüm hepsini o taşıyacak
            //Farklı sort ve gruplama işlemleri için fonksiyonlar yazmak lazım.
            if (groupBy) {
                groupMap = getCollection().getItems().stream()
                        .collect(
                                Collectors.groupingBy(
                                        x -> {
                                            switch (rafController.getSortBy()) {
                                                case SORT_BY_NAME:
                                                    return x.getTitle().substring(0, 1).toUpperCase();
                                                case SORT_BY_MIMETYPE:
                                                    return x.getMimeType();
                                                case SORT_BY_CATEGORY:
                                                    return Strings.isNullOrEmpty(x.getCategory()) ? "" : x.getCategory();
                                                case SORT_BY_TAG:
                                                    //FIXME: aslında birden fazla tag olabilir o durumda nasıl sıralama ve gruplama yapılır? Şu anda ilki sadece kontrol ediliyor.
                                                    return x.getTags().isEmpty() ? "" : x.getTags().get(0);
                                                case SORT_BY_DATE:
                                                    //FIXME: Ay Yıl, Tarih yaklaştıkça dün, bugün olmalı. Dolayısı ile sıralaması da doğru olmalı.
                                                    return sdfForSort.format(x.getCreateDate()); // DateUtils.dateToStr(x.getCreateDate());
                                                default:
                                                    return x.getName();
                                            }
                                        },
                                        Collectors.toList())
                        );
                groupMap.values().stream().forEach(l -> l.sort(new Comparator<RafObject>() {
                    @Override
                    public int compare(RafObject t1, RafObject t2) {
                        //FIXME: Ters sıra kontrolü
                        // Aslında grup içinde sıralama hep isme göre olmalı sanırım!
                        return rafController.getDescSort() ? compareTitle(t2, t1) : compareTitle(t1, t2);
                        /*
                        switch (sortBy) {
                            case SORT_BY_NAME:
                                return compareTitle(t1, t2 );
                            case SORT_BY_MIMETYPE:
                                return compareTitle(t1, t2 );
                            case SORT_BY_CATEGORY:
                                return compareTitle(t1, t2 );
                            case SORT_BY_TAG:
                                //FIXME: aslında birden fazla tag olabilir o durumda nasıl sıralama ve gruplama yapılır? Şu anda ilki sadece kontrol ediliyor.
                                return compareTitle(t1, t2 );
                            case SORT_BY_DATE:
                                //FIXME: Ay Yıl, Tarih yaklaştıkça dün, bugün olmalı. Dolayısı ile sıralaması da doğru olmalı.
                                return compareTitle(t1, t2 );
                            default:
                                return compareTitle(t1, t2 );
                        }
                         */

                    }
                }));
                groupNames.addAll(groupMap.keySet());
                groupNames.sort(new Comparator<String>() {
                    @Override
                    public int compare(String t1, String t2) {
                        //FIXME: Sıralama tipine göre buranın farklı algoritma çalıştırması lazım. Özellikle tarih sırlaması
                        //FIXME: Ters sıra kontrolü
                        return rafController.getDescSort() ? t2.compareTo(t1) : t1.compareTo(t2);
                    }
                });
            } else {
                groupNames.add("ALL");
                groupMap.clear();
                groupMap.put("ALL", getCollection().getItems().stream().sorted(new Comparator<RafObject>() {
                    @Override
                    public int compare(RafObject t1, RafObject t2) {
                        //FIXME: Tesr sıra kontrolü
                        int r = 0;
                        switch (rafController.getSortBy()) {
                            case SORT_BY_NAME:
                                return rafController.getDescSort() ? compareTitle(t2, t1) : compareTitle(t1, t2);
                            case SORT_BY_MIMETYPE:
                                return rafController.getDescSort() ? compareMimeType(t2, t1) : compareMimeType(t1, t2);
                            case SORT_BY_CATEGORY:
                                //return t1.getCategory().compareTo(t2.getCategory());
                                //FIXME: category yoksa nasıl sıralayacağız?
                                return rafController.getDescSort() ? compareCategory(t2, t1) : compareCategory(t1, t2);
                            case SORT_BY_TAG:
                                //FIXME: aslında birden fazla tag olabilir o durumda nasıl sıralama ve gruplama yapılır? Şu anda ilki sadece kontrol ediliyor.
                                return rafController.getDescSort() ? compareTag(t2, t1) : compareTag(t1, t2);
                            case SORT_BY_DATE:
                                //FIXME: Ay Yıl, Tarih yaklaştıkça dün, bugün olmalı. Dolayısı ile sıralaması da doğru olmalı.
                                return rafController.getDescSort() ? compareDate(t2, t1) : compareDate(t1, t2);
                            default:
                                return rafController.getDescSort() ? compareTitle(t2, t1) : compareTitle(t1, t2);
                        }
                    }
                }).collect(Collectors.toList()));
            }
        }
        return groupNames;
    }

    private int compareFolder(RafObject t1, RafObject t2) {
        boolean f1 = (t1 instanceof RafFolder);
        boolean f2 = (t2 instanceof RafFolder);

        if (f1 && f2) {
            return 0;
        } else if (f1 && !f2) {
            return -1;
        } else if (!f1 && f2) {
            return 1;
        } else {
            return 0;
        }
    }

    private int compareTitle(RafObject t1, RafObject t2) {
        int r = compareFolder(t1, t2);
        Collator collator = Collator.getInstance(new Locale("tr", "TR"));
        return r == 0 ? collator.compare(t1.getTitle(), t2.getTitle()) : r;
    }

    private int compareMimeType(RafObject t1, RafObject t2) {
        int r = compareFolder(t1, t2);
        return r == 0 ? t1.getMimeType().compareTo(t2.getMimeType()) : r;
    }

    /**
     * FIXME: categori sıralaması ile ilgili dert var. Kategori olmaması ile
     * ilgili
     *
     * @param t1
     * @param t2
     * @return
     */
    private int compareCategory(RafObject t1, RafObject t2) {
        int r = compareFolder(t1, t2);
        return r == 0 ? t1.getTitle().compareTo(t2.getTitle()) : r;
    }

    /**
     * FIXME: tag'a göre sıralama daha da büyük dert. Birden fazla tag olabilir.
     * Nasıl olacak?
     *
     * @param t1
     * @param t2
     * @return
     */
    private int compareTag(RafObject t1, RafObject t2) {
        int r = compareFolder(t1, t2);
        return r == 0 ? t1.getTitle().compareTo(t2.getTitle()) : r;
    }

    /**
     * //FIXME: Ay Yıl, Tarih yaklaştıkça dün, bugün olmalı. Dolayısı ile
     * sıralaması da doğru olmalı.
     *
     * @param t1
     * @param t2
     * @return
     */
    private int compareDate(RafObject t1, RafObject t2) {
        int r = compareFolder(t1, t2);
        return r == 0 ? t1.getCreateDate().compareTo(t2.getCreateDate()) : r;
    }

    public List<RafObject> getGroupItems(String group) {
        return groupMap.get(group);
    }

    public void clear() {
        groupNames.clear();
        groupMap.clear();
    }

    public Boolean getFoldersFirst() {
        return foldersFirst;
    }

    public void setFoldersFirst(Boolean foldersFirst) {
        this.foldersFirst = foldersFirst;
        clear();
    }

    public Boolean getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(Boolean groupBy) {
        this.groupBy = groupBy;
        clear();
    }

    public Boolean getShowDetails() {
        return showDetails;
    }

    public void setShowDetails(Boolean showDetails) {
        this.showDetails = showDetails;
    }

}
