package com.ozguryazilim.raf.forms.model;

import com.ozguryazilim.raf.models.RafDocument;

/**
 *
 * @author oyas
 */
public class RafDocumentField extends AbstractRafObjectSelectField<RafDocument>{

    @Override
    public Class<RafDocument> getValueClass() {
        return RafDocument.class;
    }

    @Override
    public String getType() {
        return "RafDocument";
    }
    
}
