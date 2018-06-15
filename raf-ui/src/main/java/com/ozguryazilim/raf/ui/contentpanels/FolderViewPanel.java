/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.ui.contentpanels;

import com.ozguryazilim.raf.MetadataRegistery;
import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.config.ContentPanelPages;
import com.ozguryazilim.raf.models.RafMetadata;
import com.ozguryazilim.raf.ui.base.AbstractMetadataPanel;
import com.ozguryazilim.raf.ui.base.ContentPanel;
import com.ozguryazilim.raf.ui.base.MetadataPanelRegistery;
import com.ozguryazilim.raf.ui.base.ObjectContentPanel;
import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@ContentPanel( actionIcon = "fa-folder", view = ContentPanelPages.FolderViewPanel.class )
public class FolderViewPanel extends ObjectContentPanel{
    
    private static final Logger LOG = LoggerFactory.getLogger(FolderViewPanel.class);
    
    @Inject
    private RafContext context;
    
    public List<AbstractMetadataPanel> getMetadataPanels(){
        
        //FIXME: burada veri içeriğine göre doğru bir şekilde panel listesi dönülecek.
        //FIXME: Yetki kontrolü de gerekiyor.
        
        List<AbstractMetadataPanel> result = MetadataPanelRegistery.getPanels("nt:file");
        
        for( RafMetadata md : context.getSelectedObject().getMetadatas()){
            List<AbstractMetadataPanel> ls = MetadataPanelRegistery.getPanels(md.getType());
            for( AbstractMetadataPanel mdp : ls ){
                mdp.setMetadata(md);
                result.add( mdp );
            }
        }
        
        //Sırasını düzgün hale getirelim.
        result.sort(new Comparator<AbstractMetadataPanel>() {
            @Override
            public int compare(AbstractMetadataPanel p1, AbstractMetadataPanel p2) {
                return p1.getOrder() > p2.getOrder() ? 1 : p1.getOrder() < p2.getOrder() ? -1 : 0;
            }
        });
        
        return result;
    }
    
    public List<String> getAdditonalMetadatas(){
        //FIXME: gelen veriler mecut metadata'lar ile karşılaştırılmalı.
        //FIXME: yetki kontrolü nerede yapılmalı?
        return MetadataRegistery.getSelectableMetadataNames(context.getSelectedObject().getMimeType());
    }

    /**
     * Burada olmayan bir metadata bloğu ekleniyor. Dolayısı ile doğrudan Editor açacağız.
     * @param name 
     */
    public void addMetadata( String name ){
        LOG.info("Metadata add : {}", name);
        String type = MetadataRegistery.getMetadataType(name);
        //Birden fazla UI panel regiter edilebilir dolayısı ile biz sadece ilkini açıyoruz. Select edilebildiğine göre edit de edilebilmeli!
        List<AbstractMetadataPanel> ls = MetadataPanelRegistery.getPanels(type);
        ls.get(0).edit();
    }
}
