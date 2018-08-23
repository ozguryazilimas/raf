/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.esign;

import com.ozguryazilim.muhurdar.utils.EImzaContext;
import com.ozguryazilim.muhurdar.utils.EsyaConfiguration;
import com.ozguryazilim.telve.api.module.TelveModule;
import java.io.FileNotFoundException;
import java.util.Properties;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tr.gov.tubitak.uekae.esya.api.common.ESYAException;

/**
 *
 * @author oyas
 */
@TelveModule
public class RafEsignModule {
 
    private final static Logger LOG = LoggerFactory.getLogger(RafEsignModule.class);
    
    @PostConstruct
    public void init() {
        
        //FIXME: buradaki değerler config resolverdan alınacaklar.
        
        Properties prop = new Properties();

        prop.put(EImzaContext.EIMZA_CONFIG_PATH, "res://esya-signature-config.xml");
        prop.put(EImzaContext.EIMZA_LICENCE_PATH, "res://lisans.xml");
        prop.put(EImzaContext.EIMZA_POLICY_PATH, "res://certval-policy.xml");

        EImzaContext.instance(prop);
        try {
            EsyaConfiguration.getPolicyInstance();
        } catch (ESYAException | FileNotFoundException ex) {
            LOG.error("MA3 API can not initialize", ex);
        }
    }
    
}
