/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Aslında NodeType olarak nt:folder olup içerisinde bulunan nt:file ve diğer node'lar ile birlikte tek bir belge gibi davranacak özel bir model.
 * 
 * Evrak olarak geçecek. Aslında Evrak Kayıt nesnesi. Mutlaka ek bir metadata eklentisi olacak.
 * 
 * İçereceği metadata'lar büyük ihtimal recordType:metadata ve documentType:metadata olacak.
 * 
 * @author Hakan Uygun
 */
public class RafRecord extends RafObject{
    
    private String recordType;
    private String documentType;
    
    
    private String mainDocument;
    private Boolean electronicDocument; 
    private String location;
    
    private String processId;
    private Long processIntanceId;
    
    private String status;
    
    private List<RafDocument> documents = new ArrayList<>();
    
    @Override
    public String getMimeType() {
        return RafMimeTypes.RAF_RECORD;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getMainDocument() {
        return mainDocument;
    }

    public void setMainDocument(String mainDocument) {
        this.mainDocument = mainDocument;
    }

    public Boolean getElectronicDocument() {
        return electronicDocument;
    }

    public void setElectronicDocument(Boolean electronicDocument) {
        this.electronicDocument = electronicDocument;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public Long getProcessIntanceId() {
        return processIntanceId;
    }

    public void setProcessIntanceId(Long processIntanceId) {
        this.processIntanceId = processIntanceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<RafDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<RafDocument> documents) {
        this.documents = documents;
    }
    
    
    
}
