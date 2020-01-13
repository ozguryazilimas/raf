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
 * akışı adımları.
 *
 */
@Entity
@Table(name = "EXTERNAL_DOC_WF_STEP")
public class ExternalDocWFStep extends EntityBase implements Cloneable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "genericSeq")
    @Column(name = "ID")
    private Long id;

    @Column(name = "DOCUMENT_WF_ID", length = 50, nullable = false)
    @NotNull
    @Size(min = 1, max = 50)
    private String documentWFId;

    @Column(name = "RAF_FILE_PATH", length = 1000, nullable = false)
    @NotNull
    @Size(min = 1, max = 1000)
    private String rafFilePath;

    @Column(name = "RAF_FILE_ID", length = 200, nullable = false)
    @NotNull
    @Size(min = 1, max = 200)
    private String rafFileId;

    @Column(name = "STARTED_DATE", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date startedDate;

    @Column(name = "STARTER", length = 200, nullable = true)
    @Size(min = 1, max = 200)
    private String starter;

    @Column(name = "STATE", length = 200, nullable = true)
    @Size(min = 1, max = 200)
    private String state;

    @Column(name = "COMPLETED_DATE", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date completedDate;

    @Column(name = "COMPLETER", length = 200, nullable = true)
    @Size(min = 1, max = 200)
    private String completer;

    @Column(name = "COMMENT", length = 1000, nullable = true)
    @Size(min = 1, max = 1000)
    private String comment;

    @Column(name = "STEP_NAME", length = 200, nullable = true)
    @Size(min = 1, max = 200)
    private String stepName;

    public Date getCompletedDate() {
        return completedDate;
    }

    public String getDocumentWFId() {
        return documentWFId;
    }

    public String getRafFileId() {
        return rafFileId;
    }

    public String getRafFilePath() {
        return rafFilePath;
    }

    public void setCompletedDate(Date completedDate) {
        this.completedDate = completedDate;
    }

    public void setDocumentWFId(String documentWFId) {
        this.documentWFId = documentWFId;
    }

    public void setRafFileId(String rafFileId) {
        this.rafFileId = rafFileId;
    }

    public void setRafFilePath(String rafFilePath) {
        this.rafFilePath = rafFilePath;
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

    public void setId(Long id) {
        this.id = id;
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

    public Long getId() {
        return id;
    }

    public Object clone() throws
            CloneNotSupportedException {
        return super.clone();
    }
}
