/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.ui.sidepanels;

import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.config.SidePanelPages;
import com.ozguryazilim.raf.tag.TagSuggestionService;
import com.ozguryazilim.raf.ui.base.AbstractSidePanel;
import com.ozguryazilim.raf.ui.base.SidePanel;
import java.util.List;
import javax.inject.Inject;

/**
 *
 * @author oyas
 */
@SidePanel(view = SidePanelPages.TagsSidePanel.class, icon = "fa-tags")
public class TagsSidePanel extends AbstractSidePanel{
 
    @Inject
    private RafContext context;
    
    @Inject
    private TagSuggestionService tagService;
    
    public List<String> getTags(){
        return tagService.getSuggestions(context.getSelectedRaf().getCode());
    }
    
}
