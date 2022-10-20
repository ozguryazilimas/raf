package com.ozguryazilim.raf.rest.jcr.model;

import org.modeshape.jcr.api.index.IndexDefinition;

import java.util.ArrayList;
import java.util.List;

public class IndexDefinitionPayload {
    private String name;
    private String providerName;
    private String kind;
    private String nodeTypeName;
    private String description;
    private boolean enabled;
    private boolean synchronous;
    private boolean singleColumn;
    private List<RepositoryIndexDefinitionPayload> columns = new ArrayList<>();

    public IndexDefinitionPayload(IndexDefinition indexDefinition) {
        this.name = indexDefinition.getName();
        this.providerName = indexDefinition.getProviderName();
        this.kind = String.valueOf(indexDefinition.getKind());
        this.nodeTypeName = indexDefinition.getNodeTypeName();
        this.description = indexDefinition.getDescription();
        this.enabled = indexDefinition.isEnabled();
        this.synchronous = indexDefinition.isSynchronous();
        this.singleColumn = indexDefinition.hasSingleColumn();

        for (int i = 0; i < indexDefinition.size(); i++) {
            this.columns.add(new RepositoryIndexDefinitionPayload(indexDefinition.getColumnDefinition(i)));
        }

    }

    public IndexDefinitionPayload() {
    }

    public IndexDefinitionPayload(String name, String providerName, String kind, String nodeTypeName,
                                  String description, boolean enabled, boolean synchronous, boolean singleColumn,
                                  List<RepositoryIndexDefinitionPayload> repositoryIndexDefinitionPayloads) {
        this.name = name;
        this.providerName = providerName;
        this.kind = kind;
        this.nodeTypeName = nodeTypeName;
        this.description = description;
        this.enabled = enabled;
        this.synchronous = synchronous;
        this.singleColumn = singleColumn;
        this.columns = repositoryIndexDefinitionPayloads;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getNodeTypeName() {
        return nodeTypeName;
    }

    public void setNodeTypeName(String nodeTypeName) {
        this.nodeTypeName = nodeTypeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isSynchronous() {
        return synchronous;
    }

    public void setSynchronous(boolean synchronous) {
        this.synchronous = synchronous;
    }

    public boolean isSingleColumn() {
        return singleColumn;
    }

    public void setSingleColumn(boolean singleColumn) {
        this.singleColumn = singleColumn;
    }

    public List<RepositoryIndexDefinitionPayload> getColumns() {
        return columns;
    }

    public void setColumns(List<RepositoryIndexDefinitionPayload> columns) {
        this.columns = columns;
    }
}
