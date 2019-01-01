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
