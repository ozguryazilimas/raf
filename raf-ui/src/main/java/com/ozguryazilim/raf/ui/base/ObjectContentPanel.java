/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.ui.base;

import com.ozguryazilim.raf.IconResolver;
import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.models.RafObject;
import javax.inject.Inject;

/**
 * RafObject ( yani tek bir nesne için ) content panel taban sınıfı.
 * 
 * RafFolder ya da RafDocument olabilir içerik.
 * 
 * @author Hakan Uygun
 */
public abstract class ObjectContentPanel extends AbstractContentPanel{
    
    @Inject
    private RafContext context;

    @Inject
    private IconResolver iconResolver;
    
    @Override
    public String getTitle() {
        //FIXME: Burası Title olacak
        return context.getSelectedObject().getTitle();
    }

    @Override
    public String getIcon() {
        return iconResolver.getIcon(context.getSelectedObject().getMimeType());
    }
    
    
    @Override
    public boolean getSupportPaging() {
        //Tek nesne içerikleri aslında Paging desteklemez ama view'e bırakalım.
        return Boolean.FALSE;
    }
    
    public RafObject getRafObject(){
        return context.getSelectedObject();
    }

    @Override
    public boolean getSupportCollection() {
        return false;
    }
    
    
}
