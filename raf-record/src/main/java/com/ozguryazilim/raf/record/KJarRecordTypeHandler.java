/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.record;

import com.ozguryazilim.raf.auth.KJarResourceHandler;
import java.io.InputStream;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 *
 * @author oyas
 */
@ApplicationScoped
public class KJarRecordTypeHandler implements KJarResourceHandler{

    @Inject
    private RecordTypeManager manager;
    
    @Override
    public boolean canHandle(String fileName) {
        return fileName.matches(".+rts.xml$");
    }

    @Override
    public void handle(InputStream is) {
        manager.deploy(is);
    }
    
}
