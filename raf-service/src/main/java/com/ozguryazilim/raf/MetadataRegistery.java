/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sistemde tanımlı metadata bloklarının kaydını tutar.
 * 
 * Bu metadata blokları çoğunlukla kullanıcı tarafından kullanılabilecek seçimlik metadata bloklarıdır.
 * 
 * @author Hakan Uygun
 */
public class MetadataRegistery {
 
    private static final Logger LOG = LoggerFactory.getLogger(MetadataRegistery.class);
    
    private static final Map< String, MetadataConfig> configs = new HashMap<>();
    
    
    public static void register( MetadataConfig config ){
        configs.put(config.getName(), config);
    }
    
    public static Collection<MetadataConfig> getConfigs(){
        return configs.values();
    }
    
    public static List<String> getSelectableMetadataNames(){
        return configs.values().stream().filter( c -> c.isSelectable())
                .map( c-> c.getName())
                .collect(Collectors.toList());
    }
    
    public static List<String> getSelectableMetadataNames( String mimeType ){
        return configs.values().stream().filter( c -> c.isSelectable())
                .filter(c -> c.getMimeType().equals("*") || mimeType.startsWith(c.getMimeType()))
                .map( c -> c.getName())
                .collect(Collectors.toList());
    }
    
    public static String getMetadataType( String name ){
        MetadataConfig c = configs.get(name);
        
        return c == null ? null : c.getType();
    }
}
