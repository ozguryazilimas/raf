/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.definition;

import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.entities.RafDefinition_;
import com.ozguryazilim.raf.events.RafDataChangedEvent;
import com.ozguryazilim.telve.data.RepositoryBase;
import com.ozguryazilim.telve.forms.Browse;
import com.ozguryazilim.telve.forms.BrowseBase;
import com.ozguryazilim.telve.query.QueryDefinition;
import com.ozguryazilim.telve.query.columns.TextColumn;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import org.apache.deltaspike.jpa.api.transaction.Transactional;

/**
 *
 * @author oyas
 */
@Browse( feature = RafDefinitionFeature.class )
public class RafDefinitionBrowse extends BrowseBase<RafDefinition, RafDefinition>{

    @Inject
    private RafDefinitionRepository repository;
    
    @Inject
    private Event<RafDataChangedEvent> rafDataChangedEvent;
    
    //private SuggestionItem suggestionItem;
    
    @Override
    protected void buildQueryDefinition(QueryDefinition<RafDefinition, RafDefinition> queryDefinition) {
        
        /*
        queryDefinition
                .addFilter(new StringListFilter<>(SuggestionItem_.group, SuggestionGroupRegistery.intance().getGroupNames(), "general.label.Group", "suggestionGroup.label."))
                .addFilter(new StringFilter<>(SuggestionItem_.key, "general.label.Key"));
                */
        
        //queryDefinition.addColumn(new MessageColumn<>(SuggestionItem_.group, "general.label.Group", "suggestionGroup.label." ),true);
        queryDefinition.addColumn(new TextColumn<>(RafDefinition_.code, "general.label.Code"),true);
        queryDefinition.addColumn(new TextColumn<>(RafDefinition_.name, "general.label.Name"),true);
        queryDefinition.addColumn(new TextColumn<>(RafDefinition_.info, "general.label.Info"),true);
        
    }

    @Override
    protected RepositoryBase<RafDefinition, RafDefinition> getRepository() {
        return repository;
    }

    @Transactional
    public void deleteRaf(){
        if( selectedItem != null ){
            repository.remove(selectedItem);
            search();
            
            rafDataChangedEvent.fire(new RafDataChangedEvent());
        }
    }
    
}
