package com.ozguryazilim.raf.record;

import com.ozguryazilim.raf.auth.KJarResourceHandler;
import com.ozguryazilim.raf.auth.RafAsset;
import java.io.InputStream;
import java.util.List;
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
    public void handle( String kjarId, InputStream is) {
        manager.deploy( kjarId, is);
    }

    @Override
    public void undeploy(String kjarId) {
        manager.undeploy(kjarId);
    }

    @Override
    public List<RafAsset> getAssests(String kjarId) {
        return manager.getAssests(kjarId);
    }
    
    
}
