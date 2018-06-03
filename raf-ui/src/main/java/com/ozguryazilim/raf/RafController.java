/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.definition.RafDefinitionRepository;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.telve.auth.Identity;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.core.api.scope.WindowScoped;

/**
 * Temel Raf arayüzü controller sınıfı.
 * 
 * @author Hakan Uygun
 */
@WindowScoped
@Named
public class RafController implements Serializable{
   
    @Inject
    private Identity identity;
    
    @Inject
    private RafDefinitionRepository rafDefinitionRepository;

    @Inject
    private RafContext context;
    
    //Todo plugin olarak otomatik toplanacaklar.
    @Inject
    private FolderSidePanel folderSidePanel;
    
    @Inject
    private CategorySidePanel categorySidePanel;
    
    private SidePanel selectedSidePanel;
    
    private String rafCode;
    
    private RafDefinition rafDefinition;
    
    /**
     * Sayfa çağrıldığında init olması için çağrılır.
     * ViewAction olarak
     */
    public void init(){
        //Eğer bir şey atanmamış ise kişisel raf olsun.
        if( Strings.isNullOrEmpty(rafCode)){
            rafCode = "PRIVATE";
        }
        
        //FIXME: Raf seçimi service katmanında olmalı. PRIVATE ve SHARED giri özel rafların veritabanından gelmeme gibi bir huyu var :)
        rafDefinition = rafDefinitionRepository.findAnyByCode(rafCode);
        
        context.setSelectedRaf(rafDefinition);
    }

    public String getRafCode() {
        return rafCode;
    }

    public void setRafCode(String rafCode) {
        this.rafCode = rafCode;
    }

    public RafDefinition getRafDefinition() {
        return rafDefinition;
    }

    public void setRafDefinition(RafDefinition rafDefinition) {
        this.rafDefinition = rafDefinition;
    }
    

    /**
     * Geriye SidePanel listesini döndürür.
     * 
     * FIXME: Plugin yapısı oluşacak ve otomatik toplanacaklar.
     * 
     * @return 
     */
    public List<SidePanel> getSidePanels(){
        List<SidePanel> result = new ArrayList<>();
        
        result.add(folderSidePanel);
        result.add(categorySidePanel);
        
        return result;
    }

    public SidePanel getSelectedSidePanel() {
        //Eğer seçili bir yoksa ilkini seçiyoruz.
        if( selectedSidePanel == null ){
            selectedSidePanel = getSidePanels().get(0);
        }
        return selectedSidePanel;
    }

    public void setSelectedSidePanel(SidePanel selectedSidePanel) {
        this.selectedSidePanel = selectedSidePanel;
    }
    
    
    
}
