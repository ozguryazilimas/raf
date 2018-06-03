/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import java.io.Serializable;
import javax.inject.Named;
import org.apache.deltaspike.core.api.scope.WindowScoped;

/**
 *
 * @author oyas
 */
@WindowScoped
@Named
public class CategorySidePanel implements SidePanel, Serializable{

    @Override
    public String getTitle() {
        return "Category";
    }

    @Override
    public String getIcon() {
        return "fa-sitemap";
    }

    @Override
    public String getFragment() {
        return "/fragments/categorySidePanel.xhtml";
    }
    
}
