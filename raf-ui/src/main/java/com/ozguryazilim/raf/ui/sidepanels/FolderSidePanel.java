package com.ozguryazilim.raf.ui.sidepanels;

import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.config.SidePanelPages;

import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.ui.base.AbstractSidePanel;
import com.ozguryazilim.raf.ui.base.SidePanel;

import java.util.List;
import javax.inject.Inject;


/**
 * Context'e bağlı olarak Folder listesi sunan FolderSidePanel için controller sınıfı.
 * 
 * FIXME: Folder seçildiğinde ne yapılacak? Seçili Folder UI'a nasıl yansıtılacak.
 * 
 * @author Hakan Uygun
 */
@SidePanel(view = SidePanelPages.FolderSidePanel.class, icon = "fa-folder")
public class FolderSidePanel extends AbstractSidePanel{

    @Inject
    private RafContext context;
    

    public List<RafFolder> getFolders(){
        return context.getFolders();
    }
}
