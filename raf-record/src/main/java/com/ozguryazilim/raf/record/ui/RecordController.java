package com.ozguryazilim.raf.record.ui;

import com.ozguryazilim.raf.jbpm.config.BpmPages;
import com.ozguryazilim.raf.record.RecordTypeManager;
import com.ozguryazilim.raf.record.model.RafRecordType;
import com.ozguryazilim.telve.auth.Identity;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
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
    
    @Inject
    private Identity identity;
    
    /**
     * Geriye Kullanılabilir kayıt tipleirni döndürür.
     * @return 
     */
    public List<RafRecordType> getRecordTypes(){
        //FIXME: yetki kontrolü yapılacak ve cachelenecek
        //FIXME: aktif olan record tipleri dönülecek!
        
        List<RafRecordType> result = recordTypeManager.getRecordTypes();
        
        //Aslında rol kontrolü yapıyoruz!
        result = result.stream().filter( r -> r.getPermission().equals("ALL") || identity.getRoles().contains(r.getPermission()))
                .collect(Collectors.toList());
        
        return result;
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
