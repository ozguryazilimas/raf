package com.ozguryazilim.raf.search;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.SearchService;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.models.RafCollection;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.messages.FacesMessages;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@WindowScoped
@Named
public class DetailedSearchController implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(DetailedSearchController.class);

    @Inject
    private RafDefinitionService rafDefinitionService;

    @Inject
    private Identity identity;

    @Inject
    private SearchService searchService;

    private List<RafDefinition> rafList;

    private String searchRaf;

    private String searchText;

    private Date dateFrom;

    private Date dateTo;

    public Date getDateFrom() {
        return dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public String getSearchRaf() {
        return searchRaf;
    }

    @PostConstruct
    public void init() {
        rafList = rafDefinitionService.getRafsForUser(identity.getLoginName());
    }

    public List<RafDefinition> getRafList() {
        return rafList;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    public void setRafList(List<RafDefinition> rafList) {
        this.rafList = rafList;
    }

    public void setSearchRaf(String searchRaf) {
        this.searchRaf = searchRaf;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public void search() {
        LOG.info("Search for {}", searchText);

        try {
            RafCollection c = searchService.search(searchText, rafDefinitionService.getRafDefinitionByCode(searchRaf));
            LOG.info("Results : {}", c);

        } catch (RafException ex) {
            //FIXME: i18n
            LOG.error("Search Exception", ex);
            FacesMessages.error("Sorgu yapılamadı", ex.getLocalizedMessage());
        }

        searchText = null;
    }
}
