/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.ui.base;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.IconResolver;
import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.events.RafCollectionChangeEvent;
import com.ozguryazilim.raf.models.RafCollection;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.telve.utils.DateUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * Collection tipi content panelleri için taban sınıf.
 *
 * Farklı view tipleri bu sınıfı miras alarak farklı sunum katmanları
 * oluşturabilir.
 *
 * @author Hakan Uygun
 */
public abstract class CollectionContentPanel extends AbstractContentPanel {

    private static final String SORT_BY_NAME = "NAME";
    private static final String SORT_BY_DATE = "DATE";
    private static final String SORT_BY_MIMETYPE = "MIMETYPE";
    private static final String SORT_BY_CATEGORY = "CATEGORY";
    private static final String SORT_BY_TAG = "TAG";

    @Inject
    private RafContext context;

    @Inject
    private IconResolver iconResolver;

    private String sortBy = SORT_BY_NAME;
    private Boolean descSort = Boolean.FALSE;
    private Boolean foldersFirst = Boolean.TRUE;
    private Boolean groupBy = Boolean.FALSE;

    private List<String> groupNames = new ArrayList<>();
    private Map<String, List<RafObject>> groupMap = new HashMap<>();

    @Override
    public String getTitle() {
        return context.getCollection().getTitle();
    }

    /* TODO: Sanırım üst sınıfın ki daha doğru çalışıyor.
    @Override
    public String getIcon() {
        return iconResolver.getIcon(context.getCollection().getMimeType());
    }
     */
    @Override
    public boolean getSupportPaging() {
        //Bu implemente edildiğinde açılacak
        return Boolean.FALSE;
    }

    public RafCollection getCollection() {
        return context.getCollection();
    }

    public List<String> getGroupNames() {
        if (groupNames.isEmpty()) {
            //FIXME: Burada arayüzden alınan sıralama ve gruplama yetenekleri kullanılacak

            //Eğer Gruplama gösterilmeyecek ise sadece tek bir grup olacak ve tüm hepsini o taşıyacak
            //Farklı sort ve gruplama işlemleri için fonksiyonlar yazmak lazım.
            if (groupBy) {
                groupMap = context.getCollection().getItems().stream()
                        .collect(
                                Collectors.groupingBy(
                                        x -> {
                                            switch (sortBy) {
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
                                                    return DateUtils.dateToStr(x.getCreateDate());
                                                default:
                                                    return x.getName();
                                            }
                                        },
                                        Collectors.toList())
                        );
                groupMap.values().stream().forEach(l -> l.sort(new Comparator<RafObject>() {
                    @Override
                    public int compare(RafObject t1, RafObject t2) {
                        //FIXME: FolderFirst kontrolü
                        //FIXME: Ters sıra kontrolü
                        switch (sortBy) {
                            case SORT_BY_NAME:
                                return t1.getTitle().compareTo(t2.getTitle());
                            case SORT_BY_MIMETYPE:
                                return t1.getTitle().compareTo(t2.getTitle());
                            case SORT_BY_CATEGORY:
                                return t1.getTitle().compareTo(t2.getTitle());
                            case SORT_BY_TAG:
                                //FIXME: aslında birden fazla tag olabilir o durumda nasıl sıralama ve gruplama yapılır? Şu anda ilki sadece kontrol ediliyor.
                                return t1.getTitle().compareTo(t2.getTitle());
                            case SORT_BY_DATE:
                                //FIXME: Ay Yıl, Tarih yaklaştıkça dün, bugün olmalı. Dolayısı ile sıralaması da doğru olmalı.
                                return t1.getCreateDate().compareTo(t2.getCreateDate());
                            default:
                                return t1.getTitle().compareTo(t2.getTitle());
                        }
                        
                    }
                }));
                groupNames.addAll(groupMap.keySet());
                groupNames.sort(new Comparator<String>() {
                    @Override
                    public int compare(String t1, String t2) {
                        //FIXME: Sıralama tipine göre buranın farklı algoritma çalıştırması lazım. Özellikle tarih sırlaması
                        //FIXME: Ters sıra kontrolü
                        return t1.compareTo(t2);
                    }
                });
            } else {
                groupNames.add("ALL");
                groupMap.clear();
                groupMap.put("ALL", context.getCollection().getItems().stream().sorted(new Comparator<RafObject>() {
                    @Override
                    public int compare(RafObject t1, RafObject t2) {
                        //FIXME: FolderFirst kontrolü
                        //FIXME: Tesr sıra kontrolü
                        switch (sortBy) {
                            case SORT_BY_NAME:
                                return t1.getTitle().compareTo(t2.getTitle());
                            case SORT_BY_MIMETYPE:
                                return t1.getMimeType().compareTo(t2.getMimeType());
                            case SORT_BY_CATEGORY:
                                //return t1.getCategory().compareTo(t2.getCategory());
                                //FIXME: category yoksa nasıl sıralayacağız?
                                return t1.getTitle().compareTo(t2.getTitle());
                            case SORT_BY_TAG:
                                //FIXME: aslında birden fazla tag olabilir o durumda nasıl sıralama ve gruplama yapılır? Şu anda ilki sadece kontrol ediliyor.
                                return t1.getTitle().compareTo(t2.getTitle());
                            case SORT_BY_DATE:
                                //FIXME: Ay Yıl, Tarih yaklaştıkça dün, bugün olmalı. Dolayısı ile sıralaması da doğru olmalı.
                                return t1.getCreateDate().compareTo(t2.getCreateDate());
                            default:
                                return t1.getTitle().compareTo(t2.getTitle());
                        }
                    }
                }).collect(Collectors.toList()));
            }
        }
        return groupNames;
    }

    public List<RafObject> getGroupItems(String group) {
        return groupMap.get(group);
    }

    @Override
    public boolean getSupportCollection() {
        return true;
    }

    @Override
    public String getIcon() {
        if ("raf/folder".equals(context.getCollection().getMimeType())) {
            return "fa-folder-open";
        }
        return iconResolver.getIcon(context.getCollection().getMimeType());
    }

    public void listener(@Observes RafCollectionChangeEvent event) {
        clear();
    }

    public void clear() {
        groupNames.clear();
        groupMap.clear();
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
        clear();
    }

    public Boolean getDescSort() {
        return descSort;
    }

    public void setDescSort(Boolean descSort) {
        this.descSort = descSort;
        clear();
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

}
