/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.jbpm.workitemhandler;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.models.RafCollection;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafNode;
import com.ozguryazilim.raf.models.RafObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process başladığında processe verilen belgeleri process'e özel bir alana
 * kopya alır.
 *
 * Daha sonra süreç boyunca bu dosyaları kulanılacak. Genelde en son adım olarak
 * da kullanıcı klasörlerine geri bir kopya çıkarılacak.
 *
 * Bu davranışın nedeni süreç içerisinde işleme tabii olan dosyaların kullanıcı
 * ile etkileşime girmesini engellemek.
 *
 * Bu task'ın süreç içerisinde kullanıcıya bırakılmasının nedeni ise bazı
 * süreçlerde bu işlemin yapılmasına gerek olmayışı. Örneğin sadece belgeye bir
 * onay istenen süreçlerde böyle bir duruma ihtiyaç yok.
 *
 * @author Hakan Uygun
 */
@ApplicationScoped
public class RafImportProcessDocumentsTaskHandler implements WorkItemHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RafImportProcessDocumentsTaskHandler.class);

    @Inject
    private RafService rafService;
    
    @Inject
    private RuntimeDataService runtimeDataService;

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        LOG.info("Work Item Params : {}", workItem.getParameters());

        //FIXME: Burada bir yetki problemi var. Task'ı gören kişi belgeleri göremiyor olabilir! Kontrol edilmeli.
        List<RafObject> rafObjects = new ArrayList<>();

        List<String> rafOIDs = (List<String>) workItem.getParameter("Documents");
        if (rafOIDs != null) {
            for (String oid : rafOIDs) {
                try {
                    rafObjects.add(rafService.getRafObject(oid));
                } catch (RafException ex) {
                    LOG.error("Raf Exception", ex);
                    //FIXME: burada runtime exception fırlatmak lazım sanırım. İşlem tamamlanamadı sonuçta süreç devam etmemeli!
                }
            }
        }

        Map<String,Object> resultParams = new HashMap<>();
        try {
            //TODO: processId v.s. için ek servis mi çalıştıracağız?
            ProcessInstanceDesc processInstance = runtimeDataService.getProcessInstanceById(workItem.getProcessInstanceId());
            
            
            RafFolder processFolder = getProcessFolder( processInstance.getProcessId(), workItem.getProcessInstanceId(), processInstance.getDataTimeStamp());
            rafService.copyObject(rafObjects, processFolder);
            
            RafCollection rc = rafService.getCollection(processFolder.getId());
            List<String> resultIds = new ArrayList<>();
            for( RafObject ro : rc.getItems()){
                resultIds.add(ro.getId());
            }
            
            resultParams.put("Documents", resultIds);
            
        } catch (RafException ex) {
            LOG.error("Raf Exception", ex);
            //FIXME: buarda runtime exception fırlatmak lazım sanırım. İşlem tamamlanamadı sonuçta süreç devam etmemeli!
        }

        LOG.debug("WI Result Params : {}", resultParams);
        manager.completeWorkItem(workItem.getId(), resultParams);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        //Abort sırasında yapacak bişeyimiz yok. Bu işlem zaten abort edilecek kadar beklemiyor.
    }

    private RafFolder getProcessFolder( String processId, Long processInstanceId, Date startDate ) throws RafException {
        
        RafNode processNode = rafService.getProcessRafNode();
        
        //FIXME: buarda tarih ile yıl/ay eklenecek klasör yoluna
        
        String folderPath  = processNode.getPath() + "/" + processId + "/" + processInstanceId;
        
        return rafService.createFolder(folderPath);
    }

}
