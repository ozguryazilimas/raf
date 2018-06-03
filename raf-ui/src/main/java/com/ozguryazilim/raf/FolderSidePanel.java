/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import java.io.Serializable;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.core.api.scope.WindowScoped;

/**
 *
 * @author oyas
 */
@WindowScoped
@Named
public class FolderSidePanel implements SidePanel, Serializable{

    @Inject
    private RafContext context;
    
    @Override
    public String getTitle() {
        return context.getSelectedRaf().getName();
    }

    @Override
    public String getIcon() {
        return "fa-folder";
    }

    @Override
    public String getFragment() {
        return "/fragments/folderSidePanel.xhtml";
    }
    
}
