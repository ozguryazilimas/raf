/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import com.ozguryazilim.raf.models.RafObject;
import java.io.Serializable;
import javax.inject.Inject;

/**
 *
 * @author oyas
 */
public abstract class RafObjectContentPanel implements ContentPanel, Serializable{
    
    @Inject
    private RafContext context;

    @Inject
    private IconResolver iconResolver;
    
    @Override
    public String getTitle() {
        //FIXME: Burası Title olacak
        return context.getSelectedObject().getName();
    }

    @Override
    public String getIcon() {
        return iconResolver.getIcon(context.getSelectedObject().getMimeType());
    }
    
    
    @Override
    public Boolean supportPaging() {
        //Her daim false
        return Boolean.FALSE;
    }
    
    public RafObject getRafObject(){
        return context.getSelectedObject();
    }
}
