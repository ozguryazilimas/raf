/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.jbpm.ui;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.jbpm.actions.StartProcessAction;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.telve.auth.Identity;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.model.UserTaskInstanceDesc;
import org.jbpm.services.api.query.QueryService;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@WindowScoped
@Named
public class TaskController implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(StartProcessAction.class);

    @Inject
    private Identity identity;

    @Inject
    private RuntimeDataService runtimeDataService;

    @Inject
    private UserTaskService taskService;
    
    @Inject
    private QueryService queryService;

    @Inject
    private RafService rafService;

    private Long selectedTaskId = 0l;
    private Task selectedTask;
    private Map<String, Object> taskContent;

    private List<RafObject> rafObjectItems = new ArrayList<>();

    private List<TaskAction> taskActions = new ArrayList<>();
    
    private String commentText;
    
    public List<TaskSummary> getTasks() {

        List<TaskSummary> result = runtimeDataService.getTasksAssignedAsPotentialOwner(identity.getLoginName(), null);

        return result;
    }

    public String getTaskView() {
        if (selectedTask == null) {
            return "/bpm/taskView.xhtml";
        }

        UserTaskInstanceDesc ut = runtimeDataService.getTaskById(selectedTaskId);

        String fn = ut.getFormName();

        LOG.info("Task Form Name : {}", fn);

        return "/bpm/taskView.xhtml";
    }

    public void selectTask(Long taskId) {
        selectedTaskId = taskId;
        selectedTask = taskService.getTask(taskId);
        taskContent = taskService.getTaskInputContentByTaskId(taskId);

        rafObjectItems.clear();

        //FIXME: Burada bir yetki problemi var. Task'ı gören kişi belgeleri göremiyor olabilir! Kontrol edilmeli.
        List<String> rafOIDs = (List<String>) taskContent.get("in_document");
        for (String oid : rafOIDs) {
            try {
                rafObjectItems.add(rafService.getRafObject(oid));
            } catch (RafException ex) {
                LOG.error("Raf Exception", ex);
            }
        }

        //Geri dönüş aksiyonları parse ediliyor.
        taskActions.clear();
        String tacts = (String) taskContent.get("in_result_actions");
        if( Strings.isNullOrEmpty(tacts)) {
            tacts = "";
        }
        
        List<String> ls = Splitter.on(',').trimResults().omitEmptyStrings().splitToList(tacts);
        //Eğer tanımlı bir taskAction yok ise demek ki sadece devam olanağı var.
        if( ls.isEmpty() ){
            taskActions.add( TaskActionRegistery.getAction("CONTINUE"));
        } else {
            for( String a : ls ){
                taskActions.add( TaskActionRegistery.getAction(a));
            }
        }
        
        
        
        LOG.info("Task Input Content : {}", taskContent);
    }

    public Task getSelectedTask() {
        return selectedTask;
    }

    public List<RafObject> getRafObjectItems() {
        return rafObjectItems;
    }

    public void setRafObjectItems(List<RafObject> rafObjectItems) {
        this.rafObjectItems = rafObjectItems;
    }

    public List<TaskAction> getTaskActions() {
        return taskActions;
    }

    /**
     * Verilen action'a göre HT kapatılır. 
     * 
     * Kapanış sırasında out_result ve out_document değerlerini doldurur.
     * 
     * FIXME: başka out_ değerler varsa onları nasıl handle edeceğiz?
     * 
     * @param action 
     */
    public void closeAs( String action ){
        LOG.info("Close Action : {}", action);
        //Öncelikle varsa comment'i bir yazalım
        addComment();
        
        //Yeni belge eklenmiş olabilir sürece. Dolayısı ile onları da dolduruyoruz.
        List<String> rafOIDs = new ArrayList<>();
        for( RafObject o : rafObjectItems){
            rafOIDs.add(o.getId());
        }
        
        Map<String,Object> completeParams = new HashMap<>();
        completeParams.put("out_result", action);
        completeParams.put("out_document", rafOIDs);
        
        taskService.completeAutoProgress(selectedTaskId, identity.getLoginName(), completeParams);
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public void addComment(){
        if( !Strings.isNullOrEmpty(commentText) && selectedTask != null ){
            taskService.addComment(selectedTaskId, commentText, identity.getLoginName(), new Date());
            commentText = "";
        }
    }
}
