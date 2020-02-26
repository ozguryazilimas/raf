package com.ozguryazilim.raf.jbpm.workitemhandler;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafMetadata;
import com.ozguryazilim.raf.models.RafRecord;
import java.util.HashMap;
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
 * @author oyas
 */
@ApplicationScoped
public class RafRecordExportDocumentsHandler implements WorkItemHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RafRecordExportDocumentsHandler.class);

    @Inject
    private RafService rafService;

    @Inject
    private RuntimeDataService runtimeDataService;

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        LOG.info("Work Item Params : {}", workItem.getParameters());

        String recordObjectId = (String) workItem.getParameter("recordObject");

        try {

            RafRecord recordObject = (RafRecord) rafService.getRafObject(recordObjectId);

            //TODO: processId v.s. için ek servis mi çalıştıracağız?
            ProcessInstanceDesc processInstance = runtimeDataService.getProcessInstanceById(workItem.getProcessInstanceId());

            //FIXME: RootFolder verilmemiş ise ne olacak?
            RafFolder targetFolder = (RafFolder) workItem.getParameter("targetFolder");

            LOG.debug("Process record documents {} exported to {}", recordObject, targetFolder);

            rafService.copyObject(recordObject, targetFolder);

            //FIXME: copy sırasında elimizdeki recodrObject'i değil daha önceden bişileri ( ilk HT'den ) kopyalıyor. 
            // Şimdilik sadece metadata eksik onu yeniden kopyalayıp geçici bir çözüme gidiyorum. Debug için daha fazla zaman harcayamayacağım. 
            //Sorun transaction yönetim ile ilgili olabilir. jBPM ve Modeshape karışımı nedeni ile
            // UYARI: Bu arada copyRecord'un bulunmasında isimden kaynaklı sorun olabilir. Aynı isimli bir hedef varsa dosya adı otomatik değiştiriliyor rafServis içinde.
            RafRecord copyRecord = (RafRecord) rafService.getRafObjectByPath(targetFolder.getPath() + "/" + recordObject.getName());

            copyRecord.setRecordNo(recordObject.getRecordNo());
            copyRecord.setStatus(recordObject.getStatus());
            for (RafMetadata m : recordObject.getMetadatas()) {
                RafMetadata mc = new RafMetadata();
                mc.setType(m.getType());
                mc.getAttributes().putAll(m.getAttributes());
                copyRecord.getMetadatas().add(mc);
            }

            rafService.saveRecord(copyRecord);
            //Çevresinden dolaşın buraya kadar

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
}
