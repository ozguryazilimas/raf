/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import java.io.Serializable;
import javax.inject.Inject;

/**
 *
 * @author oyas
 */
public abstract class RafObjectContentPanel implements ContentPanel, Serializable{
    
    @Inject
    private RafContext context;

    @Override
    public String getTitle() {
        return context.getCollection().getTitle();
    }

    @Override
    public String getIcon() {
        //FIXME: burada mimetype üzerinden icon almak lazım. context.getCollection().getMimeType();
        return "fa-folder-open";
    }
    
    
    @Override
    public Boolean supportPaging() {
        //Her daim false
        return Boolean.FALSE;
    }
}
