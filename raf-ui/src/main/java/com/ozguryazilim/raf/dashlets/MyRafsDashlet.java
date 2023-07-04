package com.ozguryazilim.raf.dashlets;

import com.google.common.base.Strings;
import com.ozguryazilim.mutfak.kahve.Kahve;
import com.ozguryazilim.mutfak.kahve.annotations.UserAware;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.events.RafDataChangedEvent;
import com.ozguryazilim.raf.utils.RafPathUtils;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.config.LocaleSelector;
import com.ozguryazilim.telve.dashboard.AbstractDashlet;
import com.ozguryazilim.telve.dashboard.Dashlet;
import com.ozguryazilim.telve.dashboard.DashletCapability;
import com.ozguryazilim.telve.messages.Messages;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.primefaces.model.LazyDataModel;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.text.Collator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 *
 * @author oyas
 */
@Dashlet(capability = {DashletCapability.canHide, DashletCapability.canMinimize, DashletCapability.canEdit}, permission = "public")
public class MyRafsDashlet extends AbstractDashlet {
    
    private static final String RAFS_DASHLET_PAGINATION_LIMIT = "rafs.dashlet.pagination.limit";

    @Inject
    private RafDefinitionService rafDefinitionService;

    @Inject
    private Identity identity;

    @Inject
    @UserAware
    private Kahve kahve;

    private List<RafDefinition> rafs;

    private String filter = "";
    private Boolean sortAsc = Boolean.TRUE;
    private Integer size;

    private LazyDataModel<RafDefinition> lazyRafs;
    private Locale searchLocale = Locale.forLanguageTag(ConfigResolver.getPropertyValue("searchLocale", "tr-TR"));

    @Override
    public void load() {
        size = getPaginationLimit();
        super.load(); //To change body of generated methods, choose Tools | Templates.
        rafs = rafDefinitionService.getRafsForUser(identity.getLoginName(), Boolean.TRUE);
        lazyRafs = new LazyRafDefinitionDataModel(this);
    }

    public List<RafDefinition> getRafs(int first, int pageSize) {
        //User yeni bir limit seçmiş mi?
        Integer oldSize = getPaginationLimit();
        if (oldSize != pageSize) {
            kahve.put(RAFS_DASHLET_PAGINATION_LIMIT, pageSize);
        }

        Collator collator = Collator.getInstance(LocaleSelector.instance().getLocale());
        return rafs.stream()
                .sorted((r1, r2)
                        -> sortAsc ? collator.compare(getRafName(r1).toLowerCase(LocaleSelector.instance().getLocale()), getRafName(r2).toLowerCase(LocaleSelector.instance().getLocale())) :
                                     collator.compare(getRafName(r1).toLowerCase(LocaleSelector.instance().getLocale()), getRafName(r2).toLowerCase(LocaleSelector.instance().getLocale())) * -1
                )
                .filter(r -> Strings.isNullOrEmpty(filter) || r.getName().toLowerCase(searchLocale).contains(filter.toLowerCase(searchLocale)))
                .skip(first)
                .limit(pageSize)
                .collect(Collectors.toList());
    }

    private String getRafName(RafDefinition rafDefinition) {
        if (rafDefinition.getCode().equals(RafPathUtils.PRIVATE_PATH_NAME) || rafDefinition.getCode().equals(RafPathUtils.SHARED_PATH_NAME)) {
            return Messages.getMessage(rafDefinition.getName());
        } else {
            return rafDefinition.getName();
        }
    }

    public Integer getTotalRowCount() {
        return rafs.size();
    }

    private Integer getPaginationLimit(){
        return kahve.get(RAFS_DASHLET_PAGINATION_LIMIT, 25).getAsInteger();
    }

    /**
     * Yeni raf eklenir,silinir ya da ismi değişirse dashlet'i güncelle
     *
     * @param event
     */
    public void rafDataChangedListener(@Observes RafDataChangedEvent event) {
        load();
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public Boolean getSortAsc() {
        return sortAsc;
    }

    public void setSortAsc(Boolean sortAsc) {
        this.sortAsc = sortAsc;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public LazyDataModel<RafDefinition> getLazyRafs() {
        return lazyRafs;
    }

    public void setLazyRafs(LazyDataModel<RafDefinition> lazyRafs) {
        this.lazyRafs = lazyRafs;
    }

}
