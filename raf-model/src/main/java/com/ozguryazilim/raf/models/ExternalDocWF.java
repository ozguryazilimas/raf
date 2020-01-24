package com.ozguryazilim.raf.models;

import java.io.Serializable;
import java.util.Date;

/**
 * Diğer dokuman yönetim sistemi uygulamalarından içeri alınan dokumana ait iş
 * akışı.
 *
 */
public class ExternalDocWF implements Serializable {

    private String documentId;

    private String documentWFId;

    private Date startedDate;

    private String starter;

    private String state;

    private Date completeDate;

    private String completer;

    public String getDocumentId() {
        return documentId;
    }

    public String getDocumentWFId() {
        return documentWFId;
    }

    public Date getCompleteDate() {
        return completeDate;
    }

    public String getCompleter() {
        return completer;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public void setDocumentWFId(String documentWFId) {
        this.documentWFId = documentWFId;
    }

    public void setCompleteDate(Date completeDate) {
        this.completeDate = completeDate;
    }

    public void setCompleter(String completer) {
        this.completer = completer;
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

}
