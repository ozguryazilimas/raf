package com.ozguryazilim.raf;

import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.jcr.RafModeshapeRepository;
import com.ozguryazilim.raf.models.RafCollection;
import java.io.Serializable;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 *
 * @author oyas
 */
@ApplicationScoped
public class SearchService implements Serializable{
    
    @Inject
    private RafModeshapeRepository modeshapeRepository;
    
    public RafCollection search( String searchText, RafDefinition raf ) throws RafException{
        //FIXME: yetki kontrolleri yapılmalı.
        return modeshapeRepository.getSearchCollection(searchText, raf.getNode().getPath());
    }
    
}
