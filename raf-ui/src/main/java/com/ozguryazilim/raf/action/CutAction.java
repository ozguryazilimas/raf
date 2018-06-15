/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.action;

import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;

/**
 *
 * @author oyas
 */
@Action(icon = "fa-cut", 
        capabilities = {ActionCapability.Ajax, ActionCapability.CollectionViews, ActionCapability.DetailViews, ActionCapability.MultiSelection, ActionCapability.NeedSelection},
        group = 10,
        order = 2)
public class CutAction extends AbstractAction{
    
    @Override
    protected boolean finalizeAction() {
        
        getContext().getClipboard().clear();
        getContext().getClipboard().addAll(getContext().getSeletedItems());
        getContext().setClipboardAction(this);
        
        return super.finalizeAction();
    }
}
