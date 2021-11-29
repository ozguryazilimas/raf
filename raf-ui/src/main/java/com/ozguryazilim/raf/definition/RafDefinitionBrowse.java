package com.ozguryazilim.raf.definition;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.entities.RafDefinition_;
import com.ozguryazilim.raf.events.RafDataChangedEvent;
import com.ozguryazilim.raf.member.RafMemberRepository;
import com.ozguryazilim.telve.data.RepositoryBase;
import com.ozguryazilim.telve.forms.Browse;
import com.ozguryazilim.telve.forms.BrowseBase;
import com.ozguryazilim.telve.query.QueryDefinition;
import com.ozguryazilim.telve.query.columns.TextColumn;
import com.ozguryazilim.telve.query.filters.StringFilter;
import org.apache.commons.io.FileUtils;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * Admin tarafından raf yönetim paneli. 
 * 
 * FIXME: Çok eksik var. Yetki + Konfirmasyon + JCR verilerinin de silinmesi v.b.
 * 
 * @author Hakan Uygun
 */
@Browse( feature = RafDefinitionFeature.class )
public class RafDefinitionBrowse extends BrowseBase<RafDefinition, RafDefinition>{

    private static final Logger LOGGER = LoggerFactory.getLogger(RafDefinitionBrowse.class);
    
    @Inject
    private RafDefinitionRepository repository;
    
    @Inject
    private RafMemberRepository memberRepository;
    
    @Inject
    private Event<RafDataChangedEvent> rafDataChangedEvent;
    
    @Inject
    private RafService rafService;

    private String objectId;

    private final Map<RafDefinition, Map<String, String>> rafDefinitionPropertiesMap = new HashMap<>();
    
    @Override
    protected void buildQueryDefinition(QueryDefinition<RafDefinition, RafDefinition> queryDefinition) {
        
        queryDefinition
            .addFilter(new StringFilter<>(RafDefinition_.code, "general.label.Code"))
            .addFilter(new StringFilter<>(RafDefinition_.name, "general.label.Name"))
            .addFilter(new StringFilter<>(RafDefinition_.info, "general.label.Info"));
                
        queryDefinition.addColumn(new TextColumn<>(RafDefinition_.code, "general.label.Code"),true);
        queryDefinition.addColumn(new RafLinkColumn<>(RafDefinition_.name, "general.label.Name"),true);
        queryDefinition.addColumn(new TextColumn<>(RafDefinition_.info, "general.label.Info"),true);
        
    }

    @Override
    protected RepositoryBase<RafDefinition, RafDefinition> getRepository() {
        return repository;
    }

    @Override
    public void search() {
        super.search();
        rafDefinitionPropertiesMap.clear();
    }

    @Transactional
    public void deleteRaf(){
        if( selectedItem != null ){
            
            //RefIntegrity nedeniyle önce üyeleri siliyoruz.
            memberRepository.removeByRaf(selectedItem);
            
            //şimdi de kendisini
            repository.remove(selectedItem);
            search();
            
            //FIXME: burada JCR'yi silmek lazım.
            
            
            rafDataChangedEvent.fire(new RafDataChangedEvent());
        }
    }
    
    
    public void deleteObject( String id ){
        try {
            rafService.deleteObject(id);
        } catch (RafException ex) {
            LOGGER.error("Raf Object cannot delete", ex);
        }
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public Map<String, String> getDetailsForSelectedRaf() throws RafException {
        if (selectedItem != null) {
            if (rafDefinitionPropertiesMap.get(selectedItem) == null) {
                Map<String, String> prettyAttributesMap = new HashMap<>();
                Map<String, Long> attributesMap = rafService.getRafDefinitionProperties(rafService.getCollection(selectedItem.getNodeId()).getPath());
                prettyAttributesMap.put("totalFileCount", String.valueOf(attributesMap.get("totalFileCount")));
                prettyAttributesMap.put("totalFolderCount", String.valueOf(attributesMap.get("totalFolderCount")));
                prettyAttributesMap.put("totalFileSize", FileUtils.byteCountToDisplaySize(attributesMap.get("totalFileSize")));
                prettyAttributesMap.put("totalMember", String.valueOf(memberRepository.findByRaf(selectedItem).size()));
                rafDefinitionPropertiesMap.put(selectedItem, prettyAttributesMap);
                return prettyAttributesMap;
            } else {
                return rafDefinitionPropertiesMap.get(selectedItem);
            }
        }
        return new HashMap<>();
    }
    
}
