package com.ozguryazilim.raf.ui.base;

import com.ozguryazilim.raf.models.RafObject;
import java.io.Serializable;

/**
 *
 * @author oyas
 */
public interface RafObjectViewController<R extends RafObject> extends Serializable{
    
    void setObject( R  object );
    R getObject();
    
    /**
     * Geriye Object için icon döner
     * @return 
     */
    String getIcon();
    
    /**
     * Geriye Object için başlık bilgisi döner
     * @return 
     */
    String getTitle();
    
    /**
     * Geriye sunum için kullanılacak olan ViewId'sini döndürür.
     * @return 
     */
    String getViewId();
}
