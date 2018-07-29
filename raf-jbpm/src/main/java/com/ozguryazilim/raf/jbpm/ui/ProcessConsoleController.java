/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.jbpm.ui;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.forms.FormManager;
import com.ozguryazilim.raf.forms.model.Form;
import com.ozguryazilim.raf.forms.ui.FormController;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.ui.base.DocumentsWidgetController;
import com.ozguryazilim.telve.auth.Identity;
import java.io.Serializable;
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
import org.kie.api.runtime.process.ProcessInstance;
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
    
    //URL ile geldiğinde setlenir ve ardından init kısmında kullanılır.
    private String processIntanceId = "";
    
    private Long selectedProcessIntanceId;
    
    private ProcessInstanceDesc selectedProcessInstance;
    private Map<String, Object> selectedProcessData = new HashMap<>();
    private List<RafObject> rafObjectItems = new ArrayList<>();
    private Collection<NodeInstanceDesc> processHistory;
    private Form form;
    
    
    public void init(){
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

    
    public void selectProcess( Long processInstanceId ){
        this.selectedProcessIntanceId = processInstanceId;
        this.selectedProcessInstance = runtimeDataService.getProcessInstanceById(processInstanceId);

        //Eğer aktif ise aktif değerleri alalım
        this.selectedProcessData = null;
        if( selectedProcessInstance.getState() == ProcessInstance.STATE_ACTIVE){
            this.selectedProcessData = processService.getProcessInstanceVariables(processInstanceId);
            LOG.debug("Process Vars : {} {}", selectedProcessInstance, selectedProcessData);
        }
        
        //Aktif değil ise tarihçeden alalım
        if( this.selectedProcessData == null ){
            Collection<VariableDesc> vs = runtimeDataService.getVariablesCurrentState(processInstanceId);
            LOG.debug("Vars : {}", vs);
            this.selectedProcessData = new HashMap<>();
            for( VariableDesc vd : vs ){
                this.selectedProcessData.put(vd.getVariableId(), vd.getNewValue());
            }
        }
        
        
        rafObjectItems.clear();

        //FIXME: Burada bir yetki problemi var. Task'ı gören kişi belgeleri göremiyor olabilir! Kontrol edilmeli.
        //Tarihçeden geldiğinde burada List değil String var :(
        /*
        List<String> rafOIDs = (List<String>) selectedProcessData.get("document");
        if (rafOIDs != null) {
            for (String oid : rafOIDs) {
                try {
                    rafObjectItems.add(rafService.getRafObject(oid));
                } catch (RafException ex) {
                    LOG.error("Raf Exception", ex);
                }
            }
        }
        */
        
        //UI'da göstermek için başlatılırken kullanılan formu bulalım.
        form = formManager.getForm(selectedProcessInstance.getProcessId()+"Starter");
        
        
        processHistory = runtimeDataService.getProcessInstanceFullHistoryByType(processInstanceId,  RuntimeDataService.EntryType.START, new QueryFilter(0, 100));
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
        processService.abortProcessInstance(selectedProcessIntanceId);
    }

    public List<RafObject> getRafObjectItems() {
        return rafObjectItems;
    }

    public Collection<NodeInstanceDesc> getProcessHistory() {
        return processHistory;
    }

    @Override
    public List<RafObject> getRafObjects() {
        return rafObjectItems;
    }

    
}
