/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.record.ui;

import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.forms.FormManager;
import com.ozguryazilim.raf.forms.model.Field;
import com.ozguryazilim.raf.forms.model.Form;
import com.ozguryazilim.raf.forms.ui.FormController;
import com.ozguryazilim.raf.jbpm.ProcessLookupService;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.models.RafRecord;
import com.ozguryazilim.raf.record.model.RafRecordType;
import com.ozguryazilim.raf.ui.base.DocumentsWidgetController;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.messages.FacesMessages;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.services.api.model.UserTaskInstanceDesc;
import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@SessionScoped
@Named
public class StartRecordDialog implements Serializable, FormController, DocumentsWidgetController {

    private static final Logger LOG = LoggerFactory.getLogger(StartRecordDialog.class);

    @Inject
    private FormManager formManager;

    @Inject
    private Identity identity;

    @Inject
    private ProcessService processService;

    @Inject
    private ProcessLookupService lookupService;
    
    @Inject
    private RuntimeDataService runtimeDataService;

    @Inject
    private RafContext context;
    
    

    private RafRecordType recordType;
    private Form form;

    private Map<String, Object> data = new HashMap<>();

    //Eğer dialog açılırken seçilmiş nesneler varsa onlar.
    private List<RafObject> selectedRafItems = new ArrayList<>();

    public void openDialog(RafRecordType recordType) {

        this.recordType = recordType;
        this.data.clear();
        this.selectedRafItems.clear();

        //Process Starter formları ProcessId + Starter ile başlar
        form = formManager.getForm(recordType.getForm());
        for (Field f : form.getFields()) {
            f.setData(data);
        }

        //Eğer context'e seçili bir belge var ise onun ile başlayalım
        //Burada RafDocument ya da RafRecord tipi nesneler kabul ediliyor
        for (RafObject o : context.getSeletedItems()) {
            if ((o instanceof RafDocument) || (o instanceof RafRecord)) {
                selectedRafItems.add(o);
            }
        }

        LOG.info("Record Form Contexts {}, {}", recordType, form);

        Map<String, Object> options = new HashMap<>();
        RequestContext.getCurrentInstance().openDialog(getDialogId(), options, null);
    }

    /**
     * Process için gereken bilgiler alındı ve başlatılacak.
     */
    public void closeDialog() {

        LOG.info("Record Data : {}", data);

        data.put("recordType", recordType.getName());
        data.put("initiator", identity.getLoginName());
        //Belgeleri de parametre olarak ekleyelim.
        serializeRafObjects();
        

        String processId = (String) data.get("processId");
        long processInstanceId = processService.startProcess(lookupService.getDeploymentId(processId), processId, data);

        ProcessInstanceDesc processInstance = runtimeDataService.getProcessInstanceById(processInstanceId);
        
        Long taskId = null;
        for( UserTaskInstanceDesc ut : processInstance.getActiveTasks() ){
            if( identity.getLoginName().equals(ut.getActualOwner()) ){
                //Bu task bizim kullanıcımıza ait hadi oraya zıplayalım.
                taskId = ut.getTaskId();
            }
        }
        
        //FIXME: Burada Açılan task eğer kullanıcının kendisine ait ise oraya redirect lazım.
        FacesMessages.info("Süreç başarı ile başlatıldı"); //FIXME: i18n
        
        //Geriye sonuç olarak taskId döndürüyoruz. RecordController#onRecordStarted methodunda redirect gerçekleşiyor.
        RequestContext.getCurrentInstance().closeDialog(taskId);
    }

    public void cancelDialog() {
        RequestContext.getCurrentInstance().closeDialog(null);
    }

    public String getDialogId() {
        return "/record/recordDialog";
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    
    public List<RafObject> getSelectedDocuments(){
        return selectedRafItems;
    }
    
    /**
     * Seçili olan RafObject'lerin id'lerini bir liste olarak yazacağız.
     */
    protected void serializeRafObjects(){
        
        List<String> rafOIDs = new ArrayList<>();
        for( RafObject o : selectedRafItems ){
            rafOIDs.add(o.getId());
        }
        
        data.put("initialDocuments", rafOIDs);
    }

    @Override
    public Form getForm() {
        return form;
    }

    public String getRecordTitle() {
        return recordType.getTitle();
    }

    public RafRecordType getRecordType() {
        return recordType;
    }

    @Override
    public List<RafObject> getRafObjects() {
        return selectedRafItems;
    }

}
