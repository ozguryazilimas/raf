package com.ozguryazilim.raf.jbpm.ui;

import java.io.Serializable;

public class TaskFilter implements Serializable {

    private String keyword;
    private Long processId;
    private Boolean showAll = false;
    private TaskTypes taskType = TaskTypes.ACTIVE;
    private String taskOwner;

    public String getKeyword() {
        return keyword;
    }

    public String getTaskOwner() {
        return taskOwner;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    public Boolean getShowAll() {
        return showAll;
    }

    public void setShowAll(Boolean showAll) {
        this.showAll = showAll;
    }

    public TaskTypes getTaskType() {
        return taskType;
    }

    public void setTaskOwner(String taskOwner) {
        this.taskOwner = taskOwner;
    }

    public void setTaskType(TaskTypes taskType) {
        this.taskType = taskType;
    }
}
