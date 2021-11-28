package com.ozguryazilim.raf.wopi;

import com.ozguryazilim.raf.wopi.config.WopiPages;
import com.ozguryazilim.telve.auth.Identity;
import java.io.Serializable;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.config.view.navigation.ViewNavigationHandler;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hakan
 */
@WindowScoped
@Named
public class OnlineEditorController implements Serializable{
    
    private static final Logger LOG = LoggerFactory.getLogger(OnlineEditorController.class);
    
    @Inject
    private ViewNavigationHandler viewNavigationHandler;

    @Inject
    private Identity identity;
    
    private String documentId;
    
    /**
     * Verilen ID'li belgeyi Online Edtor içerisinde açar.
     * @param documentId
     */
    public void openEditor(String documentId){
        
        this.documentId = documentId;
        
        
        
        viewNavigationHandler.navigateTo(WopiPages.OnlineEditor.class);
    }
    
    
    /**
     * Geriye WOPI için URL üretip döndürür.
     * 
     * @return 
     */
    public String getWopiUrl(){
        
        //TODO: bunların bir kısmı telve.properties'den almalı
        //TODO: bir kısmını da /hostin/deiscovery üzerinden alabiliriz sanki
        
        String wopiHost = ConfigResolver.getPropertyValue("raf.wopi.client");
        String rafHost = ConfigResolver.getPropertyValue("raf.wopi.host");
        String result = wopiHost + "?WOPISrc=" + rafHost + "/wopi/files/" + this.documentId;
        
        return result;
    }
    
    public String getToken(){
        //TODO: burada bir API Token mekanizması lazım. WopiRest tarafında da bu tokenla identity'i geri bulmak gerek.
        return identity.getLoginName();
    }
}
