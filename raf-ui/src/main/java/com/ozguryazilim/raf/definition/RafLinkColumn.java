package com.ozguryazilim.raf.definition;

import com.ozguryazilim.telve.query.columns.LinkColumn;
import javax.persistence.metamodel.SingularAttribute;

/**
 *
 * @author hakan
 */
public class RafLinkColumn<E> extends LinkColumn<E>{
    
    public RafLinkColumn( SingularAttribute<? super E, ?> attribute, String labelKey ){
        super(attribute, labelKey);
    }
    
    @Override
    public String getTemplate() {
        return "raflinkcolumn";
    }
}
