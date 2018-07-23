/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.forms.model;

import com.ozguryazilim.raf.models.RafFolder;

/**
 *
 * @author oyas
 */
public class RafFolderField extends AbstractRafObjectSelectField<RafFolder>{

    @Override
    public Class<RafFolder> getValueClass() {
        return RafFolder.class;
    }
    
}
