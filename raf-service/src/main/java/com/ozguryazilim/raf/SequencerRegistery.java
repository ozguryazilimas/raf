/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
public class SequencerRegistery {

    private static final Logger LOG = LoggerFactory.getLogger(SequencerRegistery.class);
    
    private static final List<SequencerConfig> sequencers = new ArrayList<>();
    
    /**
     * ModeShape Sequencer'ları uygulama modüllerinden register etmek için kullanılır.
     * 
     * exspression kısmında output parçası yazılmaması gerekiyor.
     * 
     * Örnek Image Sequencer için : 
     * 
     * "ImageSequencer", "image", "default://*.(gif|png|pict|jpg)/jcr:content[@jcr:data]"
     * 
     * @param name
     * @param classname
     * @param exspression 
     */
    public static void register( String name, String classname, String exspression ){
        sequencers.add( new SequencerConfig(name, classname, exspression) );
        LOG.info("Sequencer registered : {}", name);
    }

    public static List<SequencerConfig> getSequencers() {
        return sequencers;
    }

}
