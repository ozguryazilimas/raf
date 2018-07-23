/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.forms.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * Form üzerinde bulunan veriler için arayüz field verileri
 * 
 * @author oyas
 * @param <T> Serializable olan bir veri modeli. 
 */
public abstract class AbstractField<T extends Serializable> implements Field<T>{

    private String id;
    private String label;
    private String placeholder;
    private String permission;
    private Boolean required = Boolean.FALSE;
    private Boolean readonly = Boolean.FALSE;
    private T defaultValue;
    private String dataKey;
    private Map<String,Object> data;
    private T value;

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
    
    
    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String getPlaceholder() {
        return placeholder;
    }

    @Override
    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    @Override
    public String getPermission() {
        return permission;
    }

    @Override
    public void setPermission(String permission) {
        this.permission = permission;
    }

    @Override
    public Boolean getRequired() {
        return required;
    }

    @Override
    public void setRequired(Boolean required) {
        this.required = required;
    }

    @Override
    public Boolean getReadonly() {
        return readonly;
    }

    @Override
    public void setReadonly(Boolean readonly) {
        this.readonly = readonly;
    }

    @Override
    public T getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String getDataKey() {
        return dataKey;
    }

    @Override
    public void setDataKey(String dataKey) {
        this.dataKey = dataKey;
    }

    @Override
    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    @Override
    public T getValue() {
        return (T) data.get(getDataKey());
    }

    @Override
    public void setValue(T value) {
        data.put(getDataKey(), value);
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
