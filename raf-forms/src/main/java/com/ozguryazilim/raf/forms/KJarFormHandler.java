/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.forms;

import com.ozguryazilim.raf.auth.KJarResourceHandler;
import java.io.InputStream;
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
    public void handle(InputStream is) {
        manager.deployForms(is);
    }
    
}
