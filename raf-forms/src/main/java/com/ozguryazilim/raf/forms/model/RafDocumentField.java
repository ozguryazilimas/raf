/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
