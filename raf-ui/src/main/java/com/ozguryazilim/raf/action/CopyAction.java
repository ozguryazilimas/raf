/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.action;

import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;

/**
 *
 * @author oyas
 */
@Action(icon = "fa-copy", supportCollection = true )
public class CopyAction extends AbstractAction{

    @Override
    protected boolean finalizeAction() {
        
        getContext().getClipboard().clear();
        getContext().getClipboard().addAll(getContext().getSeletedItems());
        getContext().setClipboardAction(this);
        
        return super.finalizeAction();
    }
    
    
}
