package com.ozguryazilim.raf.forms;

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
public class KJarFormHandler implements KJarResourceHandler{

    @Inject
    private FormManager manager;
    
    @Override
    public boolean canHandle(String fileName) {
        return fileName.matches(".+frm.xml$");
    }

    @Override
    public void handle( String kjarId, InputStream is) {
        manager.deployForms( kjarId, is);
    }

    @Override
    public void undeploy(String kjarId) {
        manager.undeployForms(kjarId);
    }

    @Override
    public List<RafAsset> getAssests(String kjarId) {
        return manager.getAssests(kjarId);
    }
    
}
