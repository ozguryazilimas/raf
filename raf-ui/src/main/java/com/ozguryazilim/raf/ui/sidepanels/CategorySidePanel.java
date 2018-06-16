/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.ui.sidepanels;

import com.ozguryazilim.raf.category.RafCategoryService;
import com.ozguryazilim.raf.config.SidePanelPages;
import com.ozguryazilim.raf.entities.RafCategory;
import com.ozguryazilim.raf.ui.base.AbstractSidePanel;
import com.ozguryazilim.raf.ui.base.SidePanel;
import java.util.List;
import javax.inject.Inject;

/**
 * Category Side Panel kontrol sınıfı.
 * 
 * FIXME: repository değil servis kullanmalı
 * 
 * @author Hakan Uygun
 */
@SidePanel(view = SidePanelPages.CategorySidePanel.class, icon = "fa-sitemap")
public class CategorySidePanel extends AbstractSidePanel{

    @Inject
    private RafCategoryService categoryService;
    
    public List<RafCategory> getCategories(){
        return categoryService.getCategories();
    }
}
