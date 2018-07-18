/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.ui.base.metadatapanels;

import com.ozguryazilim.raf.config.MetadataPanelPages;
import com.ozguryazilim.raf.forms.model.Form;
import com.ozguryazilim.raf.forms.ui.FormController;
import com.ozguryazilim.raf.ui.base.AbstractMetadataPanel;
import com.ozguryazilim.raf.ui.base.MetadataPanel;
import java.util.Map;

/**
 * Dinamik form yapısını destekleyen metadata panel yapısı.
 * 
 * Kod ile istenildiği gibi metadata panel yapısının sağlanabilmesi için mevcur metadata panel yapısına dokunulmalayacak.
 * 
 * Önce ilgili mimeType/nodeType için bir registery varsa o kullanılacak, sonra bu panel ile bir kontrol bu da yoksa Default'a düşülecek.
 * 
 * 
 * @author Hakan Uygun
 */
@MetadataPanel(type = "*:metadata", view = MetadataPanelPages.DynaFormMetadataPanel.class, order = 10)
public class DynaFormMetadataPanel extends AbstractMetadataPanel implements FormController{

    /**
     * UI için kullanılacak form tanımı
     */
    private Form form;

    @Override
    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
    }

    @Override
    public Map<String, Object> getData() {
        return getMetadata().getAttributes();
    }

    
}
