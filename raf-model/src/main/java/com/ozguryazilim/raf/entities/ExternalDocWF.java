package com.ozguryazilim.raf.entities;

import com.ozguryazilim.telve.entities.EntityBase;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Diğer dokuman yönetim sistemi uygulamalarından içeri alınan dokumana ait iş
 * akışı.
 *
 */
@Entity
@Table(name = "EXTERNAL_DOC_WF")
public class ExternalDocWF extends EntityBase implements Cloneable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "genericSeq")
    @Column(name = "ID")
    private Long id;

    @Column(name = "DOCUMENT_ID", length = 50, nullable = false)
    @NotNull
    @Size(min = 1, max = 50)
    private String documentId;

    @Column(name = "DOCUMENT_WF_ID", length = 50, nullable = false)
    @NotNull
    @Size(min = 1, max = 50)
    private String documentWFId;

    @Column(name = "STARTED_DATE", nullable = false)
    @NotNull
    @Temporal(TemporalType.DATE)
    private Date startedDate;

    @Column(name = "STARTER", length = 200, nullable = false)
    @NotNull
    @Size(min = 1, max = 200)
    private String starter;

    @Column(name = "STATE", length = 200, nullable = false)
    @NotNull
    @Size(min = 1, max = 200)
    private String state;

    @Column(name = "COMPLETE_DATE", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date completeDate;

    @Column(name = "COMPLETER", length = 200, nullable = true)
    private String completer;

    @Column(name = "RAF_FILE_PATH", length = 1000, nullable = false)
    @NotNull
    @Size(min = 1, max = 1000)
    private String rafFilePath;

    @Column(name = "RAF_FILE_ID", length = 200, nullable = false)
    @NotNull
    @Size(min = 1, max = 200)
    private String rafFileId;

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

    public Long getId() {
        return id;
    }

    public String getRafFileId() {
        return rafFileId;
    }

    public void setId(Long id) {
        this.id = id;
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

    public void setRafFileId(String rafFileId) {
        this.rafFileId = rafFileId;
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

    public String getRafFilePath() {
        return rafFilePath;
    }

    public void setRafFilePath(String rafFilePath) {
        this.rafFilePath = rafFilePath;
    }

    public Object clone() throws
            CloneNotSupportedException {
        return super.clone();
    }
}
