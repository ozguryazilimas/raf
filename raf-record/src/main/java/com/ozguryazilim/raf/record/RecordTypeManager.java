/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.record;

import com.ozguryazilim.raf.record.model.RafRecordType;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sistemde tanımlı olan RecordType tanımlarını ve bu tanımla ilişkili nesleri yönetir.
 * 
 * @author Hakan Uygun
 */
@ApplicationScoped
public class RecordTypeManager implements Serializable{
    
    private static final Logger LOG = LoggerFactory.getLogger(RecordTypeManager.class);
    
    private Map<String, RafRecordType> typeMap = new HashMap<>();
    
    @PostConstruct
    public void init(){
        
    }
    
    /**
     * Sisteme yeni RecordType'lar register eder.
     * @param recordTypes 
     */
    public void register(List<RafRecordType> recordTypes){
        //FIXME: çakışma durumları kontrol edilmeli.
        for( RafRecordType r : recordTypes){
            typeMap.put( r.getName(), r);
            LOG.info("RecordType Registered : {}", r.getName());
            LOG.debug("RecordType Registered : {}", r);
        }
    }
    
    public void deploy( InputStream is ){
        List<RafRecordType> recordTypes = RecordTypeXmlParser.parse(is);
        register(recordTypes);
    }
 
    
    public List<RafRecordType> getRecordTypes(){
        return new ArrayList(typeMap.values());
    }
    
    
}
