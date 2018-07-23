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
import com.ozguryazilim.raf.forms.FormManager;
import com.ozguryazilim.raf.forms.model.Field;
import com.ozguryazilim.raf.forms.model.Form;
import com.ozguryazilim.raf.forms.ui.FormController;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.telve.auth.Identity;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.services.api.query.QueryService;
import org.kie.api.task.model.Comment;
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
public class TaskController implements Serializable, FormController {

    private static final Logger LOG = LoggerFactory.getLogger(TaskController.class);

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

    @Inject
    private FormManager formManager;

    private Long selectedTaskId = 0l;
    private Task selectedTask;
    private Map<String, Object> taskContent;

    private List<RafObject> rafObjectItems = new ArrayList<>();

    private List<TaskAction> taskActions = new ArrayList<>();

    //URL ile geldiğinde setlenir ve ardından init kısmında kullanılır.
    private String taskId = "";
    
    private String commentText;
    private Form form;

    public void init(){
        if( !Strings.isNullOrEmpty(taskId) ){
            //FIXME: burada exception mümkün. Kontrol etmeli
            selectTask(Long.parseLong(taskId));
        }
    }
    
    public List<TaskSummary> getTasks() {

        List<TaskSummary> result = runtimeDataService.getTasksAssignedAsPotentialOwner(identity.getLoginName(), null);

        return result;
    }

    public String getTaskView() {
        if (selectedTask == null) {
            return "/bpm/taskView.xhtml";
        }

        //FIXME: Burada custom taskView yapılabilmesine imkan vermeliyiz. E-İmza v.b. için lazım olacak!

        return "/bpm/taskView.xhtml";
    }

    public String getProcessName( String deploymentId, String processId ){
        ProcessDefinition processDesc = runtimeDataService.getProcessesByDeploymentIdProcessId(deploymentId, processId);
        return processDesc.getName();
    }
    
    public void selectTask(Long taskId) {
        selectedTaskId = taskId;
        selectedTask = taskService.getTask(taskId);
        taskContent = taskService.getTaskInputContentByTaskId(taskId);

        rafObjectItems.clear();

        //FIXME: Burada bir yetki problemi var. Task'ı gören kişi belgeleri göremiyor olabilir! Kontrol edilmeli.
        List<String> rafOIDs = (List<String>) taskContent.get("document");
        if (rafOIDs != null) {
            for (String oid : rafOIDs) {
                try {
                    rafObjectItems.add(rafService.getRafObject(oid));
                } catch (RafException ex) {
                    LOG.error("Raf Exception", ex);
                }
            }
        }

        //Geri dönüş aksiyonları parse ediliyor.
        taskActions.clear();
        String tacts = (String) taskContent.get("result_actions");
        if (Strings.isNullOrEmpty(tacts)) {
            tacts = "";
        }

        List<String> ls = Splitter.on(',').trimResults().omitEmptyStrings().splitToList(tacts);
        //Eğer tanımlı bir taskAction yok ise demek ki sadece devam olanağı var.
        if (ls.isEmpty()) {
            taskActions.add(TaskActionRegistery.getAction("CONTINUE"));
        } else {
            for (String a : ls) {
                taskActions.add(TaskActionRegistery.getAction(a));
            }
        }

        form = formManager.getForm((String)taskContent.get("TaskName"));
        for( Field f : form.getFields() ){
            f.setData(taskContent);
        }

        LOG.debug("Selected Form : {}", form);

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
    public void closeAs(String action) {
        LOG.info("Close Action : {}", action);
        //Öncelikle varsa comment'i bir yazalım
        addComment();

        //Yeni belge eklenmiş olabilir sürece. Dolayısı ile onları da dolduruyoruz.
        List<String> rafOIDs = new ArrayList<>();
        for (RafObject o : rafObjectItems) {
            rafOIDs.add(o.getId());
        }

        Map<String, Object> completeParams = new HashMap<>();
        completeParams.put("result", action);
        completeParams.put("document", rafOIDs);

        for (Map.Entry<String, Object> e : taskContent.entrySet()) {
            //Zaten tanıdık bildik bi rşey değil ise out'a dolduralım
            if (!("result".equals(e.getKey()) || "document".equals(e.getKey()) || "result_actions".equals(e.getKey()))) {
                completeParams.putIfAbsent(e.getKey(), e.getValue());
            }
        }

        taskService.completeAutoProgress(selectedTaskId, identity.getLoginName(), completeParams);

        //FIXME: eğer geriye task kalmamış ise ne olacak?
        List<TaskSummary> ls = getTasks();
        if (!ls.isEmpty()) {
            selectTask(ls.get(0).getId());
        } else {
            selectedTask = null;
        }
    }

    /**
     * Geriye task'ın bağlı olduğu process'e ait bütün tasklardan commentleri
     * toparlayıp döndürür.
     *
     * FIXME: Daha performanslı bir sorguya ihtiyaç var! FIXME: Task değişene ya
     * da etkileşene kadar comment listesini saklamak lazım
     *
     * @return
     */
    public List<Comment> getComments() {
        List<Comment> comments = new ArrayList<>();
        if (selectedTask != null) {
            List<Long> ls = runtimeDataService.getTasksByProcessInstanceId(selectedTask.getTaskData().getProcessInstanceId());
            for (Long l : ls) {
                comments.addAll(taskService.getCommentsByTaskId(l));
            }

            //Eklenme tarihine göre sıralayalım
            comments.sort(new Comparator<Comment>() {
                @Override
                public int compare(Comment t, Comment t1) {
                    return t.getAddedAt().compareTo(t1.getAddedAt());
                }
            });
        }

        return comments;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public void addComment() {
        if (!Strings.isNullOrEmpty(commentText) && selectedTask != null) {
            taskService.addComment(selectedTaskId, commentText, identity.getLoginName(), new Date());
            commentText = "";
        }
    }

    @Override
    public Form getForm() {
        return form;
    }

    @Override
    public Map<String, Object> getData() {
        return taskContent;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
    
    
}
