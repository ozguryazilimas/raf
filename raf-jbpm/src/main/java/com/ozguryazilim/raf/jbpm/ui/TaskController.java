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
import com.ozguryazilim.raf.action.FileUploadAction;
import com.ozguryazilim.raf.forms.FormManager;
import com.ozguryazilim.raf.forms.model.Field;
import com.ozguryazilim.raf.forms.model.Form;
import com.ozguryazilim.raf.forms.ui.FormController;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.models.RafRecord;
import com.ozguryazilim.raf.ui.base.DocumentsWidgetController;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.lookup.LookupSelectTuple;
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
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@WindowScoped
@Named
public class TaskController implements Serializable, FormController, DocumentsWidgetController {

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

    @Inject
    private FileUploadAction fileUploadAction;

    private Long selectedTaskId = 0l;
    private Task selectedTask;
    private Map<String, Object> taskContent;
    private Map<String, Object> data = new HashMap<>();

    private List<RafObject> rafObjectItems = new ArrayList<>();

    private List<TaskAction> taskActions = new ArrayList<>();
    private RafRecord recordObject;

    //URL ile geldiğinde setlenir ve ardından init kısmında kullanılır.
    private String taskId = "";

    private String commentText;
    private Form form;

    public void init() {
        if (!Strings.isNullOrEmpty(taskId)) {
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

    public String getProcessName(String deploymentId, String processId) {
        ProcessDefinition processDesc = runtimeDataService.getProcessesByDeploymentIdProcessId(deploymentId, processId);
        return processDesc.getName();
    }

    public void selectTask(Long taskId) {
        selectedTaskId = taskId;
        selectedTask = taskService.getTask(taskId);
        taskContent = taskService.getTaskInputContentByTaskId(taskId);

        data.clear();
        rafObjectItems.clear();
        recordObject = null;

        //FIXME: Burada bir yetki problemi var. Task'ı gören kişi belgeleri göremiyor olabilir! Kontrol edilmeli.
        List<String> rafOIDs = (List<String>) taskContent.get("documents");
        if (rafOIDs != null) {
            for (String oid : rafOIDs) {
                try {
                    rafObjectItems.add(rafService.getRafObject(oid));
                } catch (RafException ex) {
                    LOG.error("Raf Exception", ex);
                }
            }
        }

        //Eğer task içinden RafRecord çıkıyor ise onu ekleyelim.
        String recordObjectId = (String) taskContent.get("recordObject");
        if (!Strings.isNullOrEmpty(recordObjectId)) {
            try {
                recordObject = (RafRecord) rafService.getRafObject(recordObjectId);
            } catch (RafException ex) {
                LOG.error("Raf Exception", ex);
            }
            if (recordObject != null) {
                rafObjectItems.add(recordObject);
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

        //Form bulunuyor. eğer RafRecord için işlem yapıyor isek formu aslında record üzerinden gelen DocumentType, RecordType ile ilişkili formu bulmalı.
        //FIXME: burada RecordTypeManager yok! Form bilgisini nasıl alacağız? Şimdilik recordType üzerinden namingConvention yapsak?
        String recordType = (String) taskContent.get("recordType");
        String documentType = (String) taskContent.get("documentType");
        String taskName = (String) taskContent.get("TaskName");

        //FIXME: buarda aslında önce recordTye + documentType + taskName olmadı recordTye + taskName olmadı taskName şeklinde form aramak lazım.
        if (!Strings.isNullOrEmpty(recordType)) {
            form = formManager.getForm(recordType + "." + taskName);
        } else {
            form = formManager.getForm(taskName);
        }

        //FIXME: Burada aslında gelen verileri flat hale getirecek bir şeyler düşünmek lazım. taskContent içinde form için kullanılacak alanlar olacak.
        //Bu işlemden emin değilim
        data.putAll(taskContent);

        //Eğer metadata pass edildi ise bunları data alanına yerleştiriyoruz! Form verileri metadata mapi ile akacak
        Map<String, Object> metadata = (Map<String, Object>) taskContent.get("metadata");
        if (metadata != null) {
            data.putAll(metadata);
        }

        for (Field f : form.getFields()) {
            f.setData(data);
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
        completeParams.put("documents", rafOIDs);

        //TODO: metadata yapısı ile sanki buna gerek yok artık! ( Acaba sadece süreci ilgilendiren şeyler nasıl olacak? )
        //Bütün metadata keyleri içerisinde ':' barındıracak. Onları süreç değişkeni olarak koymayalım. ex: raf:topic 
        //FIXME: ana nodu ilgilendiren şeyleri nasıl ayıracağız? raf: ile başlıypor ise ana nodu ilgilendiriyordur!
        for (Map.Entry<String, Object> e : data.entrySet()) {
            //Zaten tanıdık bildik bir şey değil ise out'a dolduralım
            if (!e.getKey().contains(":")) {
                completeParams.putIfAbsent(e.getKey(), e.getValue());
            }
        }

        //Data alanında olan herşeyi metadata bloğuna koyuyoruz.
        completeParams.put("metadata", data);

        
        //Kullanıcı yöneticisini bulup response'a koyalım ki bir sonraki adım yönetici içinse ona düşsün.
        //FIXME: Eğer kullanıcının yöneticisi yok ise merkezi bir kullanıcıya düşürsek mi? Boşta kalan şeylerin yöneticisi şeklinde?
        completeParams.put("manager", identity.getUserInfo().getManager());
        
        
        LOG.debug("Task Complete Params : {}", completeParams);
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
        return data;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    /**
     * Documents Widget için belge listesi
     *
     * @return
     */
    @Override
    public List<RafObject> getRafObjects() {
        return rafObjectItems;
    }

    /**
     * FIXME: şimdilik sadece recordObject varsa onun içine upload'a izin
     * veriyoruz. Ve kavram olarkda sadece tek record object var olarak
     * düşünüyoruz. Aslında TaskController açısından doğru değil!
     *
     * @return
     */
    @Override
    public Boolean getCanUpload() {
        return recordObject != null;
    }

    @Override
    public void upload() {
        fileUploadAction.execute("PROCESS", recordObject.getPath());
    }

    @Override
    public void onUploadComplete() {
        //TODO: Eğer recordObject yoksa burası çağırılmaz varsayımı ile dabranıyor. Aslında NPE kontrolü yapılmalı.
        try {
            refreshRecordObject();
        } catch (RafException ex) {
            LOG.error("Raf Exception", ex);
        }
    }

    @Override
    public Boolean getCanAdd() {
        return recordObject != null;
    }

    /**
     * Lookup Dialog üzerinden seçilen değer event içerisinde gelecek ve onu
     * field value olarak yazacağız.
     *
     * @param event
     */
    public void onAddDocumentSelect(SelectEvent event) {

        LookupSelectTuple sl = (LookupSelectTuple) event.getObject();
        if (sl == null) {
            return;
        }

        if (sl.getValue() instanceof RafObject) {
            RafObject doc = (RafObject) sl.getValue();
            try {
                rafService.copyObject(doc, recordObject);
                refreshRecordObject();
            } catch (RafException ex) {
                LOG.error("Dosya kopyalanamadı", ex);
            }
        }
    }

    @Override
    public void addDocument() {
        LOG.debug("Raflardan seçim!");
    }

    protected void refreshRecordObject() throws RafException {
        LOG.debug("Raf Record Path : {}", recordObject.getPath());
        recordObject = (RafRecord) rafService.getRafObject(recordObject.getId());

        //FIXME: aslında doğru bir yöntem değil. Başka nesneler varsa sorun çıkarır. Tek nesnenin RafRecord olduğu varsayımı ile hareket ediyor.
        rafObjectItems.clear();
        rafObjectItems.add(recordObject);
    }

}
