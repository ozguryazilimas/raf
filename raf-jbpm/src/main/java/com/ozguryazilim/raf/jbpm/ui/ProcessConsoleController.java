package com.ozguryazilim.raf.jbpm.ui;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.ReadOnlyModeService;
import com.ozguryazilim.raf.forms.FormManager;
import com.ozguryazilim.raf.forms.model.Field;
import com.ozguryazilim.raf.forms.model.Form;
import com.ozguryazilim.raf.forms.ui.FormController;
import com.ozguryazilim.raf.jbpm.summaries.GetBAMTaskSummariesByProcessIntanceCommand;
import com.ozguryazilim.raf.models.RafMetadata;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.models.RafRecord;
import com.ozguryazilim.raf.ui.base.DocumentsWidgetController;
import com.ozguryazilim.telve.auth.Identity;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.NodeInstanceDesc;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.services.api.model.VariableDesc;
import org.jbpm.services.task.audit.impl.model.BAMTaskSummaryImpl;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.internal.query.QueryFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Yetkiye bağlı olarak açık olan process listesinin yönetilmesini sağlar.
 * 
 * Bu aynı zamanda kişilerin ellerindeki işleri de takip edebilmesine olanak verecek.
 * @author Hakan Uygun
 */
@WindowScoped
@Named
public class ProcessConsoleController implements Serializable, FormController, DocumentsWidgetController{
    
    private static final Logger LOG = LoggerFactory.getLogger(ProcessConsoleController.class);
    
    @Inject
    private Identity identity;

    @Inject
    private RuntimeDataService runtimeDataService;
    
    @Inject 
    private ProcessService processService;
    
    @Inject
    private FormManager formManager;
    
    @Inject
    private RafService rafService;
    
    @Inject
    private transient TaskService taskService;

    @Inject
    private ReadOnlyModeService readOnlyModeService;

    //URL ile geldiğinde setlenir ve ardından init kısmında kullanılır.
    private String processIntanceId = "";
    
    private Long selectedProcessIntanceId;
    
    private ProcessInstanceDesc selectedProcessInstance;
    private Map<String, Object> selectedProcessData = new HashMap<>();
    private List<RafObject> rafObjectItems = new ArrayList<>();
    private RafRecord recordObject;
    private Collection<NodeInstanceDesc> processHistory;
    private List<BAMTaskSummaryImpl> bamSummary;
    private Form form;
    //Bu değerler URLEncoded şeklinde tutuluyor!
    private String deploymentId;
    private String processId;
    
    public void init() throws UnsupportedEncodingException{
        if( !Strings.isNullOrEmpty(processIntanceId) ){
            //FIXME: burada exception mümkün. Kontrol etmeli
            selectProcess(Long.parseLong(processIntanceId));
        }
    }
    
    public Collection<ProcessInstanceDesc> getProcessInstances(){
        //FIXME: Burada pagination ve filtreler ile uğraşılacak! Şu hali ile sadece initiator ve active olanlar geliyor!
        List<Integer> states = new ArrayList<>();
        states.add(ProcessInstance.STATE_ACTIVE);
        Collection<ProcessInstanceDesc> instances = runtimeDataService.getProcessInstances( states, identity.getLoginName(), new QueryFilter( 0, 100));
        
        return instances;
    }

    
    public void selectProcess( Long processInstanceId ) throws UnsupportedEncodingException{
        this.selectedProcessIntanceId = processInstanceId;
        this.selectedProcessInstance = runtimeDataService.getProcessInstanceById(processInstanceId);
        
        
        this.deploymentId = URLEncoder.encode(selectedProcessInstance.getDeploymentId(), "UTF-8");
        this.processId = URLEncoder.encode(selectedProcessInstance.getProcessId(), "UTF-8");

        //Eğer aktif ise aktif değerleri alalım
        this.selectedProcessData = null;
        if( selectedProcessInstance.getState() == ProcessInstance.STATE_ACTIVE){
            //Geriye dönen UnmodifialbleMap olduğu için yeni bir taneye aktarıyoruz.
            this.selectedProcessData = new HashMap<>();
            this.selectedProcessData.putAll( processService.getProcessInstanceVariables(processInstanceId));
            LOG.debug("Process Vars : {} {}", selectedProcessInstance, selectedProcessData);
        }
        
        boolean fromHistory = false;
        
        //Aktif değil ise tarihçeden alalım
        if( this.selectedProcessData == null ){
            Collection<VariableDesc> vs = runtimeDataService.getVariablesCurrentState(processInstanceId);
            LOG.debug("Vars : {}", vs);
            this.selectedProcessData = new HashMap<>();
            for( VariableDesc vd : vs ){
                this.selectedProcessData.put(vd.getVariableId(), vd.getNewValue());
            }
            fromHistory = true;
        }
        
        Map<String,Object> metadata = new HashMap<>();
        
        
        rafObjectItems.clear();

        //FIXME: Burada bir yetki problemi var. Task'ı gören kişi belgeleri göremiyor olabilir! Kontrol edilmeli.
        //Tarihçeden geldiğinde burada List değil String var :( Parse etmek gerekir.
        /*
        List<String> rafOIDs = (List<String>) selectedProcessData.get("documents");
        if (rafOIDs != null) {
            for (String oid : rafOIDs) {
                try {
                    rafObjectItems.add(rafService.getRafObject(oid));
                } catch (RafException ex) {
                    LOG.error("Raf Exception", ex);
                }
            }
        }*/
        
        //Eğer task içinden RafRecord çıkıyor ise onu ekleyelim.
        String recordObjectId = (String) selectedProcessData.get("recordObject");
        if (!Strings.isNullOrEmpty(recordObjectId)) {
            try {
                recordObject = (RafRecord) rafService.getRafObject(recordObjectId);
            } catch (RafException ex) {
                LOG.error("Raf Exception", ex);
            }
            if (recordObject != null) {
                rafObjectItems.add(recordObject);
                
                //Tarihçeden okundu ise değerler string olarak geldi. Map değil ve veri eksiği var. O yüzden record üzerinden alalım bilgileri.
                if( fromHistory ){
                    
                    for( RafMetadata m : recordObject.getMetadatas()){
                        metadata.putAll(m.getAttributes());
                    }
                    
                    metadata.put("raf:recordNo", recordObject.getRecordNo());
                    metadata.put("raf:location", recordObject.getLocation());
                    metadata.put("raf:status", recordObject.getStatus());
                    
                    selectedProcessData.put("metadata", metadata);
                } else {
                    metadata = (Map<String, Object>) selectedProcessData.getOrDefault("metadata", new HashMap<>());
                }
            }
        }

        //FIXME: Record yapısı değil normal süreç çalışır ise bu yaptığımız işler patlar. Buna daha doğru bir çözüm bulmak gerek!
        //Metadata alanlarını flat hale getiriyoruz.
        selectedProcessData.putAll(metadata);
        
        
        //FIXME: RecordType ve DocumentType bilgilerini düzeltelim. Ama nasıl? Bu bilgiler raf-record tarafında!
        
        //UI'da göstermek için tüm process'e özel olarak hazırlanmış bir form alalım
        form = formManager.getForm(selectedProcessInstance.getProcessId());
        for (Field f : form.getFields()) {
            f.setData(selectedProcessData);
        }
        
        processHistory = runtimeDataService.getProcessInstanceFullHistoryByType(processInstanceId,  RuntimeDataService.EntryType.START, new QueryFilter(0, 100));
        
        bamSummary = taskService.execute(new GetBAMTaskSummariesByProcessIntanceCommand(processInstanceId));
        
        LOG.debug("BAM list : {}", bamSummary);
    }
    
    public String getProcessIntanceId() {
        return processIntanceId;
    }

    public void setProcessIntanceId(String processIntanceId) {
        this.processIntanceId = processIntanceId;
    }

    @Override
    public Form getForm() {
        return form;
    }

    @Override
    public Map<String, Object> getData() {
        return selectedProcessData;
    }

    public ProcessInstanceDesc getSelectedProcessInstance() {
        return selectedProcessInstance;
    }

    public Map<String, Object> getSelectedProcessData() {
        return selectedProcessData;
    }

    /**
     * Seçili olan process'i iptal eder.
     */
    public void abortProcess(){
        if (!isProcessManipulationEnabled()) {
            return;
        }

        processService.abortProcessInstance(selectedProcessIntanceId);
        resetSelectedProcessInstance();
    }

    public void resetSelectedProcessInstance() {
        try {
            selectProcess(selectedProcessIntanceId);
        } catch (UnsupportedEncodingException e) {
            selectedProcessInstance = null;
            selectedProcessIntanceId = null;
        }
    }

    public List<RafObject> getRafObjectItems() {
        return rafObjectItems;
    }

    public Collection<NodeInstanceDesc> getProcessHistory() {
        return processHistory;
    }

    public List<BAMTaskSummaryImpl> getBamSummary() {
        return bamSummary;
    }

    @Override
    public List<RafObject> getRafObjects() {
        return rafObjectItems;
    }


    public String getDeploymentId() {
        return deploymentId;
    }

    public String getProcessId() {
        return processId;
    }

    public boolean isProcessManipulationEnabled() {
        return !readOnlyModeService.isEnabled();
    }

}
