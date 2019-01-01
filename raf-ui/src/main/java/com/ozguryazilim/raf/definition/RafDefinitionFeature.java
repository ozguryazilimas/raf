package com.ozguryazilim.raf.definition;

import com.ozguryazilim.raf.config.SettingsPages;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.telve.feature.AbstractFeatureHandler;
import com.ozguryazilim.telve.feature.Feature;
import com.ozguryazilim.telve.feature.Page;
import com.ozguryazilim.telve.feature.PageType;


/**
 *
 * @author oyas
 */
@Feature(caption = "module.caption.RafDefinition", permission = "rafDefinitionAdmin", forEntity = RafDefinition.class)
@Page(type = PageType.BROWSE, page = SettingsPages.RafDefinitionBrowse.class)
public class RafDefinitionFeature extends AbstractFeatureHandler{
    
}
