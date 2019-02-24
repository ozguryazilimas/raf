package com.ozguryazilim.raf.record.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * RecordType tanım modeli.
 * 
 * @author Hakan Uygun
 */
public class RafRecordType implements Serializable{
   
    private String name;
    private String title;
    private String metadata;
    private String form;
    private Integer order = 0;
    private String permission;
    
    private List<RafRecordDocumentType> documentTypes = new ArrayList<>();
    private List<RafRecordProcess> processes = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public List<RafRecordDocumentType> getDocumentTypes() {
        return documentTypes;
    }

    public void setDocumentTypes(List<RafRecordDocumentType> documentTypes) {
        this.documentTypes = documentTypes;
    }

    public List<RafRecordProcess> getProcesses() {
        return processes;
    }

    public void setProcesses(List<RafRecordProcess> processes) {
        this.processes = processes;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
    
    

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RafRecordType other = (RafRecordType) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RafRecordType{" + "name=" + name + ", title=" + title + ", metadata=" + metadata + ", form=" + form + '}';
    }
    
    
}
