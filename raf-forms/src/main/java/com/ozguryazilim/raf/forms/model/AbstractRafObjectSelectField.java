package com.ozguryazilim.raf.forms.model;

import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.telve.lookup.LookupSelectTuple;
import org.primefaces.event.SelectEvent;

/**
 *
 * @author oyas
 */
public abstract class AbstractRafObjectSelectField< T extends RafObject> extends AbstractField<T>{

    private T value;

    /**
     * Lookup Dialog üzerinden seçilen değer event içerisinde gelecek ve onu field value olarak yazacağız.
     * @param event 
     */
    public void onSelect(SelectEvent event) {
        
        LookupSelectTuple sl = (LookupSelectTuple) event.getObject();
        if (sl == null) {
            return;
        }

        if( sl.getValue() instanceof RafObject ){
            setValue((T)sl.getValue());
        }
    }
    
}
