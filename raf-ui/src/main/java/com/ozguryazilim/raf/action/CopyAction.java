package com.ozguryazilim.raf.action;

import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;

/**
 *
 * @author oyas
 */
@Action(icon = "fa-copy", 
        capabilities = {ActionCapability.Ajax, ActionCapability.CollectionViews, ActionCapability.DetailViews, ActionCapability.MultiSelection, ActionCapability.NeedSelection},
        group = 10,
        order = 1
)
public class CopyAction extends AbstractAction{

    @Override
    protected boolean finalizeAction() {
        
        getContext().getClipboard().clear();
        getContext().getClipboard().addAll(getContext().getSeletedItems());
        getContext().setClipboardAction(this);
        
        return super.finalizeAction();
    }
    
    
}
