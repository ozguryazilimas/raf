package com.ozguryazilim.raf.ui.base;

import com.ozguryazilim.raf.models.RafCollection;
import java.io.Serializable;

/**
 * Raf Collection tipi içerik göstermek için API.
 * @author oyas
 */
public interface RafCollectionViewController extends Serializable{
    
    void setCollection( RafCollection collection );
    RafCollection getCollection();
    
    /**
     * Geriye Collection için icon döner
     * @return 
     */
    String getIcon();
    
    /**
     * Geriye collection için başlık bilgisi döner
     * @return 
     */
    String getTitle();
    
    /**
     * Geriye sunum için kullanılacak olan ViewId'sini döndürür.
     * @return 
     */
    String getViewId();
}
