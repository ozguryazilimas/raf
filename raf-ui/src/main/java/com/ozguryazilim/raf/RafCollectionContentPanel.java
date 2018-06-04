/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import com.ozguryazilim.raf.models.RafCollection;
import java.io.Serializable;
import javax.inject.Inject;

/**
 * Collection tipi content panelleri için taban sınıf.
 * 
 * Farklı view tipleri bu sınıfı miras alarak farklı sunum katmanları oluşturabilir.
 * 
 * @author Hakan Uygun
 */
public abstract class RafCollectionContentPanel implements ContentPanel, Serializable{
 
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
        //Bu implemente edildiğinde açılacak
        return Boolean.FALSE;
    }
 
    public RafCollection getCollection(){
        return context.getCollection();
    }
    
}
