package com.ozguryazilim.raf.entities;

import com.ozguryazilim.telve.entities.EntityBase;
import java.util.Date;
import java.util.logging.Logger;
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
 * Diğer dokuman yönetim sistemi uygulamalarından içeri alınan dokuman.
 *
 */
@Entity
@Table(name = "EXTERNAL_DOC")
public class ExternalDoc extends EntityBase implements Cloneable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "genericSeq")
    @Column(name = "ID")
    private Long id;
    private static final Logger LOG = Logger.getLogger(ExternalDoc.class.getName());

    @Column(name = "DOCUMENT_TYPE", length = 200, nullable = false)
    @NotNull
    @Size(min = 1, max = 200)
    private String documentType;

    @Column(name = "DOCUMENT_ID", length = 50, nullable = false)
    @NotNull
    @Size(min = 1, max = 50)
    private String documentId;

    @Column(name = "DOCUMENT_NAME", length = 200, nullable = false)
    @NotNull
    @Size(min = 1, max = 200)
    private String documentName;

    @Column(name = "DOCUMENT_CREATE_DATE", nullable = false)
    @NotNull
    @Temporal(TemporalType.DATE)
    private Date documentCreateDate;

    @Column(name = "DOCUMENT_CREATOR", length = 200, nullable = false)
    @NotNull
    @Size(min = 1, max = 200)
    private String documentCreator;

    @Column(name = "DOCUMENT_FORMAT", length = 50, nullable = false)
    @NotNull
    @Size(min = 1, max = 50)
    private String documentFormat;

    @Column(name = "DOCUMENT_FOLDER", length = 200, nullable = false)
    @NotNull
    @Size(min = 1, max = 200)
    private String documentFolder;

    @Column(name = "DOCUMENT_PARENT_FOLDER", length = 200, nullable = false)
    @NotNull
    @Size(min = 1, max = 200)
    private String documentParentFolder;

    @Column(name = "RAF_FILE_PATH", length = 1000, nullable = false)
    @NotNull
    @Size(min = 1, max = 1000)
    private String rafFilePath;

    @Column(name = "RAF_FILE_ID", length = 200, nullable = false)
    @NotNull
    @Size(min = 1, max = 200)
    private String rafFileId;

    public String getDocumentType() {
        return documentType;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getDocumentName() {
        return documentName;
    }

    public Date getDocumentCreateDate() {
        return documentCreateDate;
    }

    public String getDocumentCreator() {
        return documentCreator;
    }

    public String getDocumentFormat() {
        return documentFormat;
    }

    public String getDocumentFolder() {
        return documentFolder;
    }

    public String getDocumentParentFolder() {
        return documentParentFolder;
    }

    public Long getId() {
        return id;
    }

    public String getRafFileId() {
        return rafFileId;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public void setDocumentCreateDate(Date documentCreateDate) {
        this.documentCreateDate = documentCreateDate;
    }

    public void setDocumentCreator(String documentCreator) {
        this.documentCreator = documentCreator;
    }

    public void setDocumentFormat(String documentFormat) {
        this.documentFormat = documentFormat;
    }

    public void setDocumentFolder(String documentFolder) {
        this.documentFolder = documentFolder;
    }

    public void setDocumentParentFolder(String documentParentFolder) {
        this.documentParentFolder = documentParentFolder;
    }

    public String getRafFilePath() {
        return rafFilePath;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRafFileId(String rafFileId) {
        this.rafFileId = rafFileId;
    }

    public void setRafFilePath(String rafFilePath) {
        this.rafFilePath = rafFilePath;
    }

    public Object clone() throws
            CloneNotSupportedException {
        return super.clone();
    }
}
