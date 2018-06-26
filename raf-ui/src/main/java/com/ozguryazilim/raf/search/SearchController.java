/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.search;

import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.SearchService;
import com.ozguryazilim.raf.events.RafCollectionChangeEvent;
import com.ozguryazilim.raf.models.RafCollection;
import com.ozguryazilim.telve.messages.FacesMessages;
import java.io.Serializable;
import javax.enterprise.event.Event;
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
public class SearchController implements Serializable{
    
    private static final Logger LOG = LoggerFactory.getLogger(SearchController.class);
    
    @Inject
    private RafContext context;
    
    @Inject
    private SearchService searchService;
    
    @Inject
    private Event<RafCollectionChangeEvent> rafCollectionChangeEvent;
    
    private String searchText;

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

        
    public void search(){
        LOG.info("Search for {}", searchText);
        
        try {
            RafCollection c = searchService.search(searchText, context.getSelectedRaf());
            LOG.info("Results : {}", c);
            
            context.setCollection(c);
            
            rafCollectionChangeEvent.fire(new RafCollectionChangeEvent());
            
        } catch (RafException ex) {
            //FIXME: i18n
            LOG.error("Search Exception", ex);
            FacesMessages.error("Sorgu yapılamadı", ex.getLocalizedMessage());
        }
        
        searchText = null;
    }
}
