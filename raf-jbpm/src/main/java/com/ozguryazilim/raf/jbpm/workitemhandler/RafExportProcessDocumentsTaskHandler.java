package com.ozguryazilim.raf.jbpm.workitemhandler;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
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
 *
 * Process içinde bulunan belgeleri başka bir raf'a taşır.
 *
 * 3 adet paremetre gelir. * Documents içerisinde neler kopyalanacak *
 * RootFolder içinde hangi foldera kopyalanacak * NewFolder içinde bir alt
 * folder açılacak ise onun ismi yoksa RootFolder'a kopyalanır.
 *
 *
 * @author Hakan Uygun
 */
@ApplicationScoped
public class RafExportProcessDocumentsTaskHandler implements WorkItemHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RafExportProcessDocumentsTaskHandler.class);

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

        Map<String, Object> resultParams = new HashMap<>();
        try {
            //TODO: processId v.s. için ek servis mi çalıştıracağız?
            ProcessInstanceDesc processInstance = runtimeDataService.getProcessInstanceById(workItem.getProcessInstanceId());

            //FIXME: RootFolder verilmemiş ise ne olacak?
            String folderPath = (String) workItem.getParameter("RootFolder");
            String newFolder = (String) workItem.getParameter("NewFolder");
            if (!Strings.isNullOrEmpty(newFolder)) {
                folderPath = folderPath + "/" + newFolder;
            }

            LOG.debug("Process documents {} exported to {}", rafOIDs, folderPath);
            RafFolder targetFolder = getTargetFolder(folderPath);
            rafService.copyObject(rafObjects, targetFolder);

        } catch (RafException ex) {
            LOG.error("Raf Exception", ex);
            //FIXME: buarda runtime exception fırlatmak lazım sanırım. İşlem tamamlanamadı sonuçta süreç devam etmemeli!
        }

        Map<String, Object> metadata = (Map<String, Object>) workItem.getParameter("metadata");

        Map<String, Object> result = new HashMap<>();
        result.put("metadata", metadata);

        if (workItem.getParameter("departman") != null) {
            result.put("departman", workItem.getParameter("departman"));
        }
        if (workItem.getParameter("uzman") != null) {
            result.put("uzman", workItem.getParameter("uzman"));
        }
        if (workItem.getParameter("departmanlar") != null) {
            result.put("departmanlar", workItem.getParameter("departmanlar"));
        }

        //Geriye dönecek bir bilgimiz yok!
        manager.completeWorkItem(workItem.getId(), result);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        //Abort için özel bir işlemimiz yok!
    }

    private RafFolder getProcessFolder(String processId, Long processInstanceId, Date startDate) throws RafException {

        RafNode processNode = rafService.getProcessRafNode();

        //FIXME: buarda tarih ile yıl/ay eklenecek klasör yoluna
        String folderPath = processNode.getPath() + "/" + processId + "/" + processInstanceId;

        return rafService.createFolder(folderPath);
    }

    private RafFolder getTargetFolder(String folderPath) throws RafException {
        return rafService.createFolder(folderPath);
    }
}
