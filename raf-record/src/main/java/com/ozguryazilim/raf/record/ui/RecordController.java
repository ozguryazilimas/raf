/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.record.ui;

import com.ozguryazilim.raf.jbpm.config.BpmPages;
import com.ozguryazilim.raf.record.RecordTypeManager;
import com.ozguryazilim.raf.record.model.RafRecordType;
import java.io.Serializable;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.core.api.config.view.navigation.NavigationParameterContext;
import org.apache.deltaspike.core.api.config.view.navigation.ViewNavigationHandler;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evrak Kayıt Menü yapısı ve temel işlemleri kontrol eder.
 * 
 * Kullanıcı yetkilerine bağlı olarak, Kullanılabilecek kayıt servislerinin listesini ve yeni kayıt açma işlemlerini yönetir.
 * 
 * @author Hakan Uygun
 */
@WindowScoped
@Named
public class RecordController implements Serializable{
    
    private static final Logger LOG = LoggerFactory.getLogger(RecordController.class);
    
    @Inject
    private RecordTypeManager recordTypeManager;
    
    @Inject
    private StartRecordDialog startRecordDialog;

    @Inject
    private NavigationParameterContext navigationParameterContext;
    
    @Inject
    private ViewNavigationHandler vnh;
    
    /**
     * Geriye Kullanılabilir kayıt tipleirni döndürür.
     * @return 
     */
    public List<RafRecordType> getRecordTypes(){
        //FIXME: yetki kontrolü yapılacak ve cachelenecek
        //FIXME: aktif olan record tipleri dönülecek!
        return recordTypeManager.getRecordTypes();
    }
    
    
    public void startRecord( RafRecordType recordType){
        LOG.debug("New Record will be started for : {}", recordType);
        startRecordDialog.openDialog(recordType);
    }
    
    /**
     * Start dialoğu kapandığında çağrılır.
     * 
     * Event içerisinde eğer TaskId varsa fodğrudan TaskConsole'a yönlendirilir.
     * @param event 
     */
    public void onRecordStarted(SelectEvent event){
        Long taskId = (Long) event.getObject();
        if( taskId != null ){
            navigationParameterContext.addPageParameter("tid", taskId);
            vnh.navigateTo(BpmPages.TaskConsole.class);
        }
    }
    
}
