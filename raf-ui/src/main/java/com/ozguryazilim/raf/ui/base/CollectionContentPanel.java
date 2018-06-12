/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.ui.base;

import com.ozguryazilim.raf.IconResolver;
import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.models.RafCollection;
import javax.inject.Inject;

/**
 * Collection tipi content panelleri için taban sınıf.
 * 
 * Farklı view tipleri bu sınıfı miras alarak farklı sunum katmanları oluşturabilir.
 * 
 * @author Hakan Uygun
 */
public abstract class CollectionContentPanel extends AbstractContentPanel{
 
    @Inject
    private RafContext context;

    
    @Inject
    private IconResolver iconResolver;
    
    @Override
    public String getTitle() {
        return context.getCollection().getTitle();
    }

    @Override
    public String getIcon() {
        return iconResolver.getIcon(context.getCollection().getMimeType());
    }
    
    
    @Override
    public boolean getSupportPaging() {
        //Bu implemente edildiğinde açılacak
        return Boolean.FALSE;
    }
 
    public RafCollection getCollection(){
        return context.getCollection();
    }

    @Override
    public boolean getSupportCollection() {
        return true;
    }
    
    
}
