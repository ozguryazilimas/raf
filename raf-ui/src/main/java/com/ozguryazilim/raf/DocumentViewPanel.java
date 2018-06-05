/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import javax.inject.Named;
import org.apache.deltaspike.core.api.scope.WindowScoped;

/**
 *
 * @author oyas
 */
@WindowScoped
@Named
public class DocumentViewPanel extends RafObjectContentPanel{

    @Override
    public String getFragment() {
        return "/fragments/documentView.xhtml";
    }

    @Override
    public String getCommandIcon() {
        return "fa-file";
    }

    @Override
    public String getCommandTitle() {
        return "Detail View";
    }
    
}
