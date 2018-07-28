/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.jbpm.workitemhandler;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.models.RafMetadata;
import com.ozguryazilim.raf.models.RafRecord;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gelen parametrelerden yola çıkarak, verileri jcr üzerine işler.
 * 
 * FIXME: burada naming için düzenleme yapılmalı
 * 
 * @author Hakan Uygun
 */
@ApplicationScoped
public class RafServiceTaskHandler implements WorkItemHandler{

    private static final Logger LOG = LoggerFactory.getLogger(RafServiceTaskHandler.class);
    
    @Inject
    private RafService rafService;
    
    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        LOG.info("Work Item Params : {}", workItem.getParameters()); 
        
        //FIXME: type check ve NPE kontrolleri yapılmalı!
        Map<String,Object> metadata = (Map<String,Object>) workItem.getParameter("metadata");
        String recordObjectId = (String) workItem.getParameter("recordObject");
        
        try {
            RafRecord recordObject = (RafRecord) rafService.getRafObject(recordObjectId);
            
            recordObject.setStatus("COMPLETE"); //FIXME: bunun burada setlenmesi ne kadar doğru acaba?
            
            /*
                içerisinde ":" geçen keyleri alıyoruz
                - raf: ve jcr: ile başlayanları temizleyelim
                xxx:yyy biçminde olan keylerin xxx:metadata olan nodunu bulalım ve ona attribute olarak ekleyelim.
            */
            for( Map.Entry<String,Object> e : metadata.entrySet()){
                
                if( e.getKey().contains(":") && !e.getKey().startsWith("raf:") && !e.getKey().startsWith("jcr:")){
                    String k = e.getKey();
                    String[] mi = k.split(":");
                    String p = mi[0] + ":metadata";
                    
                    RafMetadata m = getRafMetadata(p, recordObject);
                    m.getAttributes().put(k, e.getValue());
                    
                }
            }
            
            rafService.saveRecord(recordObject);
            
        } catch (RafException ex) {
            LOG.debug("Raf Exception", ex);
            //FIXME: buarda runtime exception fırlatmak lazım sanırım. İşlem tamamlanamadı sonuçta süreç devam etmemeli!
        }
        
        
        //Geriye bişi dönmeyeceğiz
        manager.completeWorkItem(workItem.getId(), null);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        //NŞA'da abort olmamalı!
        LOG.warn("Work Abort!");
    }
    
    protected RafMetadata getRafMetadata( String name, RafRecord record ){
       return record.getMetadatas().stream()
                .filter( m -> m.getType().equals(name))
                .findFirst()
                .orElseGet( () ->{
                    RafMetadata r = new RafMetadata();
                    r.setType(name);
                    record.getMetadatas().add(r);
                    return r;
                });
    }
    
}
