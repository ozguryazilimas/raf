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

    @Override
    public String getType() {
        return "RafFolder";
    }
    
}
