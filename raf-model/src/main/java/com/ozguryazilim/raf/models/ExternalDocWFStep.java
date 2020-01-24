package com.ozguryazilim.raf.models;

import java.io.Serializable;
import java.util.Date;

/**
 * Diğer dokuman yönetim sistemi uygulamalarından içeri alınan dokumana ait iş
 * akışı adımları.
 *
 */
public class ExternalDocWFStep implements Serializable {

    private String documentWFId;

    private Date startedDate;

    private String starter;

    private String state;

    private Date completedDate;

    private String completer;

    private String comment;

    private String stepName;

    public Date getCompletedDate() {
        return completedDate;
    }

    public String getDocumentWFId() {
        return documentWFId;
    }

    public void setCompletedDate(Date completedDate) {
        this.completedDate = completedDate;
    }

    public void setDocumentWFId(String documentWFId) {
        this.documentWFId = documentWFId;
    }

    public Date getStartedDate() {
        return startedDate;
    }

    public void setStartedDate(Date startedDate) {
        this.startedDate = startedDate;
    }

    public String getStarter() {
        return starter;
    }

    public void setStarter(String starter) {
        this.starter = starter;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCompleter() {
        return completer;
    }

    public void setCompleter(String completer) {
        this.completer = completer;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

}
