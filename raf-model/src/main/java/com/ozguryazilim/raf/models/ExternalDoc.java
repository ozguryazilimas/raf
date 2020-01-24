package com.ozguryazilim.raf.models;

import java.io.Serializable;
import java.util.Date;

/**
 * Diğer dokuman yönetim sistemi uygulamalarından içeri alınan dokuman.
 *
 */
public class ExternalDoc implements Serializable {

    private String documentType;

    private String documentId;

    private String documentName;

    private Date documentCreateDate;

    private String documentCreator;

    private String documentFormat;

    private String documentFolder;

    private String documentParentFolder;

    private String documentStatus;

    public String getDocumentStatus() {
        return documentStatus;
    }

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

    public void setDocumentStatus(String documentStatus) {
        this.documentStatus = documentStatus;
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

}
