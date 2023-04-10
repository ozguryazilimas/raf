package com.ozguryazilim.raf.ui.base;

import com.ozguryazilim.raf.IconResolver;
import com.ozguryazilim.raf.RafController;
import com.ozguryazilim.raf.events.RafCheckInEvent;
import com.ozguryazilim.raf.events.RafFolderDataChangeEvent;
import com.ozguryazilim.raf.models.RafCollection;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.ui.utils.RafCollectionGrouper;
import com.ozguryazilim.raf.ui.utils.RafCollectionSorter;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.event.Event;

/**
 * RafCollection için temel view controller sınıfı.
 * <p>
 * Tek görevi Aldığı sort ve group bilgilerine göre nesneleri sıralamak ve
 * gruplamak.
 *
 * @author Hakan Uygun
 */
public abstract class AbstractRafCollectionViewController implements RafCollectionViewController {

    @Inject
    private IconResolver iconResolver;

    @Inject
    private RafController rafController;

    private RafCollection collection;

    private Boolean foldersFirst = Boolean.TRUE;
    private Boolean groupBy = Boolean.FALSE;
    private Boolean showDetails = Boolean.FALSE;

    private List<String> groupNames = new ArrayList<>();
    private Map<String, List<RafObject>> groupMap = new LinkedHashMap<>();

    @Inject
    private Event<RafFolderDataChangeEvent> folderDataChangeEvent;

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
        if (foldersFirst) {
            RafCollectionSorter.sort(rafController.getSortBy(), rafController.getDescSort(), foldersFirst, getCollection());
        }
    }

    public void checkInListener(@Observes RafCheckInEvent event) {
        if (collection != null && !collection.getItems().isEmpty() && event.getCheckedInObject() != null && event.getNewVersion() != null) {
            int index = collection.getItems().indexOf(event.getCheckedInObject());

            collection.getItems().get(index).setUpdateBy(event.getNewVersion().getCreatedBy());
            collection.getItems().get(index).setUpdateDate(event.getNewVersion().getCreated());
            collection.getItems().get(index).setVersion(event.getNewVersion().getName());
        }
    }

    public List<String> getGroupNames() {
        if (groupNames.isEmpty()) {
            if (groupBy) {
                // Gruplama yaparken UI'da "Önce Klasörler" seçilmiş olsa bile burada false veriyoruz.
                // Çünkü sıralama'da hep içerisinde klasör barındıran gruplar öne geçiyor diğer türlü.
                RafCollectionSorter.sort(rafController.getSortBy(), rafController.getDescSort(), false, getCollection());
                groupMap = RafCollectionGrouper.groupBy(rafController.getSortBy(), getCollection());
            } else {
                RafCollectionSorter.sort(rafController.getSortBy(), rafController.getDescSort(), foldersFirst, getCollection());
                groupMap.clear();
                groupMap.put("ALL", getCollection().getItems());
            }
            groupNames.addAll(groupMap.keySet());
        }
        return groupNames;
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

    public Boolean hasPreview(RafObject rafObject) {
        if (rafObject instanceof RafFolder) {
            return false;
        } else if (rafObject instanceof RafDocument) {
            return ((RafDocument) rafObject).getHasPreview();
        } else {
            return false;
        }
    }

    public void nextPage() {
        rafController.nextPage();
        folderDataChangeEvent.fire(new RafFolderDataChangeEvent());
    }
}
