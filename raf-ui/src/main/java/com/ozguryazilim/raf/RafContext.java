/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.models.RafCollection;
import com.ozguryazilim.raf.models.RafObject;
import java.io.Serializable;
import javax.inject.Named;
import org.apache.deltaspike.core.api.scope.WindowScoped;

/**
 * Kullanıcı arayüzü içerisinde seçili olan temel bileşenleri tutar.
 * 
 * Çalışma sürecinde alt bileşenler tarafından kullanılır.
 * 
 * @author Hakan Uygun
 */
@WindowScoped
@Named
public class RafContext implements Serializable{
    
    private RafDefinition selectedRaf;
    private String path;
    private RafCollection collection;
    private RafObject selectedObject;
            

    public RafDefinition getSelectedRaf() {
        return selectedRaf;
    }

    public void setSelectedRaf(RafDefinition selectedRaf) {
        this.selectedRaf = selectedRaf;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public RafCollection getCollection() {
        return collection;
    }

    public void setCollection(RafCollection collection) {
        this.collection = collection;
    }

    public RafObject getSelectedObject() {
        return selectedObject;
    }

    public void setSelectedObject(RafObject selectedObject) {
        this.selectedObject = selectedObject;
    }
    

    
}
