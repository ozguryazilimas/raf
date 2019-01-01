package com.ozguryazilim.raf.ui.base;

import com.ozguryazilim.raf.models.RafFolder;

/**
 * RafFolder detay görünümü için taban kontroller.
 * 
 * Şu anda üzerinde miras aldığı Metadata kısmı var.
 * 
 * TODO: Rule Eklenecek. Metadata kontrolü belki de düzenlenecek!
 * 
 * @author Hakan Uygun
 */
public class AbstractRafFolderViewController extends AbstractRafObjectViewController<RafFolder>{

    @Override
    public String getViewId() {
        return "/fragments/folderView.xhtml";
    }
    
}
