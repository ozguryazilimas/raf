package com.ozguryazilim.raf.rest.jcr.model;

import org.modeshape.jcr.api.PropertyType;
import org.modeshape.jcr.api.index.IndexColumnDefinition;

public class RepositoryIndexDefinitionPayload {
    private String propertyName;
    private String columnType;

    public RepositoryIndexDefinitionPayload() {
    }

    public RepositoryIndexDefinitionPayload(String propertyName, String columnType) {
        this.propertyName = propertyName;
        this.columnType = columnType;
    }

    public RepositoryIndexDefinitionPayload(IndexColumnDefinition columnDefinition) {
        this.propertyName = columnDefinition.getPropertyName();
        this.columnType = PropertyType.nameFromValue(columnDefinition.getColumnType());
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }
}