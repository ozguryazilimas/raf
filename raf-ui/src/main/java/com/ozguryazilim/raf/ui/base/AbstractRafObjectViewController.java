package com.ozguryazilim.raf.ui.base;

import com.ozguryazilim.raf.IconResolver;
import com.ozguryazilim.raf.MetadataRegistery;
import com.ozguryazilim.raf.models.RafMetadata;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.ui.base.metadatapanels.BasicMetadataPanel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RafNesneleri gösterimi için taban sınıf.
 * 
 * 
 * Bütün RafObject'ler için metadata panel desteği lazım olacaktır diye metadata panel kısmı burada.
 * 
 * TODO: Acava MetaDataAware bişimi yapmalı?
 * 
 * Benzar durum Comment için de gerekebilir!
 * 
 * @author Hakan Uygun
 */
public abstract class AbstractRafObjectViewController<R extends RafObject> implements RafObjectViewController<R>{

    private static final Logger LOG = LoggerFactory.getLogger(AbstractRafDocumentViewController.class);
    
    @Inject
    private IconResolver iconResolver;
    
    @Inject
    private BasicMetadataPanel basicMetadataPanel;
    
    private R object;
    
    @Override
    public void setObject(R object) {
        this.object = object;
    }

    @Override
    public R getObject() {
        return object;
    }

    @Override
    public String getIcon() {
        return iconResolver.getIcon(getObject().getMimeType());
    }

    @Override
    public String getTitle() {
        return getObject().getTitle();
    }
    
    public List<AbstractMetadataPanel> getMetadataPanels(){
        
        //FIXME: burada veri içeriğine göre doğru bir şekilde panel listesi dönülecek.
        //FIXME: burada yapılan işlem cachelenmeli. Ama nasıl onu bilemiyoruz. Window/Session Scope bir nesnede ikide bir değer değişecek. Belki de mimeType'a + metaData bloğu için map tutmak?
        //FIXME: Yetki kontrolü de gerekiyor.
        
        List<AbstractMetadataPanel> result = new ArrayList<>();//MetadataPanelRegistery.getPanels("nt:file");
        
        basicMetadataPanel.setObject(getObject());
        
        result.add(basicMetadataPanel);
        addCustomMetadataPanel( result );
        
        for( RafMetadata md : getObject().getMetadatas()){
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
    
    /**
     * Alt sınıflar tarafından custom ek panel eklemek için kullanılır.
     * @param list 
     */
    protected void addCustomMetadataPanel( List<AbstractMetadataPanel> list ){
        
    }
    
    public List<String> getAdditonalMetadatas(){
        //FIXME: gelen veriler mecut metadata'lar ile karşılaştırılmalı.
        //FIXME: yetki kontrolü nerede yapılmalı?
        return MetadataRegistery.getSelectableMetadataNames(getObject().getMimeType());
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
