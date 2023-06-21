package com.ozguryazilim.raf.optionpane;

import com.ozguryazilim.raf.CollectionDefaultOptionsService;
import com.ozguryazilim.raf.config.SettingsPages;
import com.ozguryazilim.raf.enums.SortType;
import com.ozguryazilim.telve.config.AbstractOptionPane;
import com.ozguryazilim.telve.config.OptionPane;
import com.ozguryazilim.telve.messages.FacesMessages;
import org.apache.deltaspike.jpa.api.transaction.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

@OptionPane(permission = "PUBLIC", optionPage = SettingsPages.CollectionOptionPane.class)
public class CollectionOptionPane extends AbstractOptionPane {

    @Inject
    private CollectionDefaultOptionsService collectionDefaultOptionsService;

    private SortType defaultSortType;
    private boolean defaultAscSort;

    @PostConstruct
    void init() {
        defaultSortType = collectionDefaultOptionsService.getDefaultSortType();
        defaultAscSort = collectionDefaultOptionsService.getDefaultDescSort();
    }

    @Override
    @Transactional
    public void save() {
        collectionDefaultOptionsService.setDefaultDescSort(defaultAscSort);
        collectionDefaultOptionsService.setDefaultSortType(defaultSortType);
        FacesMessages.info("collectionOptionPane.message.success");
    }

    public SortType getDefaultSortType() {
        return defaultSortType;
    }

    public void setDefaultSortType(SortType defaultSortType) {
        this.defaultSortType = defaultSortType;
    }

    public boolean getDefaultAscSort() {
        return defaultAscSort;
    }

    public void setDefaultAscSort(boolean defaultAscSort) {
        this.defaultAscSort = defaultAscSort;
    }

    public List<SortType> getSortTypes() {
        return Arrays.asList(SortType.values());
    }
}
