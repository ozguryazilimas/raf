/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.forms.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Form üzerinde bulunan veriler için arayüz field verileri
 * 
 * @author oyas
 * @param <T> Serializable olan bir veri modeli. 
 */
public abstract class AbstractField<T extends Serializable> implements Serializable{

    private String id;
    private String label;
    private String placeholder;
    private String permission;
    private Boolean required = Boolean.FALSE;
    private Boolean readonly = Boolean.FALSE;
    private T defaultValue;
    private String dataKey;

    public AbstractField() {
    }

    public AbstractField(String dataKey, String label) {
        this.id = dataKey;
        this.dataKey = dataKey;
        this.label = label;
    }

    public AbstractField(String id, String dataKey, String label ) {
        this.id = id;
        this.label = label;
        this.dataKey = dataKey;
    }

    public AbstractField(String dataKey) {
        this.id = dataKey;
        this.dataKey = dataKey;
    }
    
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Boolean getReadonly() {
        return readonly;
    }

    public void setReadonly(Boolean readonly) {
        this.readonly = readonly;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDataKey() {
        return dataKey;
    }

    public void setDataKey(String dataKey) {
        this.dataKey = dataKey;
    }
    
    public abstract Class<T> getValueClass();

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.id);
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
        final AbstractField<?> other = (AbstractField<?>) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
    
    
    
}
