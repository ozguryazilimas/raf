package com.ozguryazilim.raf.ui.base;

import com.google.common.base.CaseFormat;
import com.ozguryazilim.raf.forms.FormManager;
import com.ozguryazilim.raf.forms.model.Form;
import com.ozguryazilim.raf.ui.base.metadatapanels.DefaultMetadataPanel;
import com.ozguryazilim.raf.ui.base.metadatapanels.DynaFormMetadataPanel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
public class MetadataPanelRegistery {
    
    private static final Logger LOG = LoggerFactory.getLogger(MetadataPanelRegistery.class);
 
    private static final Map<String, MetadataPanel> panels = new HashMap<>();
    
    /**
     * nodeType ( metadata ) panel mappingi
     * Bir tip için birden fazla panel register edilebilir.
     */
    private static final Map<String, List<String>> typeMap = new HashMap<>();
    
    
    public static void register( String name, MetadataPanel a ){
        name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
        panels.put(name, a);
        
        List<String> l = typeMap.get(a.type());
        if( l == null ){
            l = new ArrayList<>();
            typeMap.put(a.type(),l);
        }
        
        l.add(name);
        
        LOG.info("MetadataPanel Registered : {}", name);
    }
    
    public static List<AbstractMetadataPanel> getPanels(){
        List<AbstractMetadataPanel> result = new ArrayList<>();
        for( String pn : panels.keySet()){
            result.add((AbstractMetadataPanel) BeanProvider.getContextualReference( pn, true));
        }
        
        return result;
    }
    
    public static List<AbstractMetadataPanel> getPanels( String type ){
        List<AbstractMetadataPanel> result = new ArrayList<>();
        
        List<String> l = typeMap.get(type);
        
        if( l != null ){
            for( String pn : l){
                result.add((AbstractMetadataPanel) BeanProvider.getContextualReference( pn, true));
            }
        } else {
            
            //Doğrudan bir sınıf tanımlı değil imiş. Şimdi dinamik form tanımı var mı bir bakalım!
            FormManager formManager = BeanProvider.getContextualReference(FormManager.class, true);
            Form form = formManager.getForm(type);
            if( form != null ){
                //FIXME: MetadataPanel yapısını kesinlikle refactor etmek gerek!
                //FIXME: Burada intance ile ilgili bir problem olacak. Eğer aynı tip için birden fazla form tanımlanmış ya da ise ne olacak?
                //Daha ağır bir problem var! Farklı metadata tipleri için form tanımlanmış olabilir ama elimizde sadece bir tane MetadataPanel instance'ı var ve form bilgilisni setlediğimizde sonuncusu için geçerli olacak.
                //Çünkü dialog frameworkü ile çalışmak için scope session yapılmış.
                //Keza çağrıldığı yerde de metadata veri bloğu ekleniyor bu kontroller nesnesine.
                //Metadata tipi için bir adet form almak formKey açısından da makul ama iki farklı metada data bloğu için dynaform yapısı elimizde patlar.
                DynaFormMetadataPanel mp = BeanProvider.getContextualReference( DynaFormMetadataPanel.class, true);
                mp.setForm(form);
                result.add( mp );
            } else {
                //FIXME: Default olanı döndürmek lazım ama instance ile ilgli sorun var :( Sadece bir tane olabilir.
                //FIXME: Aslında bu daha çok debug tadında bişi. Configden alsak a açık olup olmayacağını Eğer tanımlı bir panel yoksa o metadata bloğu gösterilmez.
                result.add((AbstractMetadataPanel) BeanProvider.getContextualReference( DefaultMetadataPanel.class, true));
            }
        }
        
        return result;
    }
}
