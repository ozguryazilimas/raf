package com.ozguryazilim.raf.jbpm.workitemhandler;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafNode;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.models.RafRecord;
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
 * @author oyas
 */
@ApplicationScoped
public class RafRecordImportDocumentsHandler implements WorkItemHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RafRecordImportDocumentsHandler.class);

    @Inject
    private RafService rafService;

    @Inject
    private RuntimeDataService runtimeDataService;

    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        LOG.info("Work Item Params : {}", workItem.getParameters());

        //FIXME: Burada bir yetki problemi var. Task'ı gören kişi belgeleri göremiyor olabilir! Kontrol edilmeli.
        List<RafObject> rafObjects = new ArrayList<>();

        //Öncelikle initial belge var ise bunları bir toparlayalım.
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

            RafFolder processFolder = getProcessFolder(processInstance.getProcessId(), workItem.getProcessInstanceId(), processInstance.getDataTimeStamp());

            //Burada bir RafRecord nesnesi hazırlıyoruz öncelikle.
            RafRecord record = new RafRecord();
            record.setName(workItem.getParameter("subject").toString().replaceAll("\\.", ""));
            record.setTitle(workItem.getParameter("subject").toString().replaceAll("\\.", ""));
            //TODO: Bundan pek emin değilim. Süreç için girilen açıklamanın belge için olması gerekmez.
            record.setInfo((String) workItem.getParameter("description"));

            //RafRecord'un pathini belirleyelim.
            record.setPath(processFolder.getPath() + "/" + record.getName());
            record.setRecordType((String) workItem.getParameter("recordType"));
            record.setDocumentType((String) workItem.getParameter("documentType"));

            record.setProcessId(processInstance.getProcessId());
            record.setProcessIntanceId(workItem.getProcessInstanceId());

            /* Önce belgenin kopyalanması lazım. Ve fakat bu belge her kopyalandığında sorun çıkacak.*/
            //eğer belge varsa ilkini main document olarak bağlayalım. Id kullanamıyoruz çünkü her kopyada değişecek. Daha temiz olan aslında mainDocument'i işeretlemek olacak sanırım.
            if (!rafObjects.isEmpty()) {
                record.setMainDocument(rafObjects.get(0).getName());
            }

            record = rafService.createRecord(record);

            rafService.copyObject(rafObjects, record);

            //Geri dönecek Documents bigisini boşaltalım
            List<String> resultIds = new ArrayList<>();
            resultParams.put("Documents", resultIds);

            resultParams.put("recordObject", record.getId());

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

    private RafFolder getProcessFolder(String processId, Long processInstanceId, Date startDate) throws RafException {

        RafNode processNode = rafService.getProcessRafNode();

        //FIXME: buarda tarih ile yıl/ay eklenecek klasör yoluna
        String folderPath = processNode.getPath() + "/" + processId + "/" + processInstanceId;

        return rafService.createFolder(folderPath);
    }
}
