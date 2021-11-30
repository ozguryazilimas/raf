package com.ozguryazilim.raf.dashlets;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.google.common.base.Strings;

import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.events.RafDataChangedEvent;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.dashboard.AbstractDashlet;
import com.ozguryazilim.telve.dashboard.Dashlet;
import com.ozguryazilim.telve.dashboard.DashletCapability;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import java.util.stream.Collectors;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.primefaces.model.LazyDataModel;

/**
 *
 * @author oyas
 */
@Dashlet(capability = {DashletCapability.canHide, DashletCapability.canMinimize, DashletCapability.canEdit}, permission = "public")
public class MyRafsDashlet extends AbstractDashlet {

    @Inject
    private RafDefinitionService rafDefinitionService;

    @Inject
    private Identity identity;

    @Inject
    private RafService rafService;

    private List<RafDefinition> rafs;

    private String filter = "";
    private Boolean sortAsc = Boolean.TRUE;
    private Integer size = 25;

    private LazyDataModel<RafDefinition> lazyRafs;
    private Locale searchLocale = Locale.forLanguageTag(ConfigResolver.getPropertyValue("searchLocale", "tr-TR"));

    @Override
    public void load() {
        super.load(); //To change body of generated methods, choose Tools | Templates.
        rafs = rafDefinitionService.getRafsForUser(identity.getLoginName(), Boolean.TRUE);
        lazyRafs = new LazyRafDefinitionDataModel(this);
    }

    public List<RafDefinition> getRafs() {

        return rafs.stream()
                .sorted((r1, r2)
                        -> sortAsc ? r1.getName().compareTo(r2.getName()) : r1.getName().compareTo(r2.getName()) * -1
                )
                .filter(r -> Strings.isNullOrEmpty(filter) ? true : r.getName().toLowerCase(searchLocale).contains(filter.toLowerCase(searchLocale)))
                .limit(size)
                .collect(Collectors.toList());
    }

    public List<RafDefinition> getRafs(int page, int pageSize) {

        return rafs.stream()
                .sorted((r1, r2)
                        -> sortAsc ? r1.getName().compareTo(r2.getName()) : r1.getName().compareTo(r2.getName()) * -1
                )
                .filter(r -> Strings.isNullOrEmpty(filter) ? true : r.getName().toLowerCase(searchLocale).contains(filter.toLowerCase(searchLocale)))
                .skip(page * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
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
