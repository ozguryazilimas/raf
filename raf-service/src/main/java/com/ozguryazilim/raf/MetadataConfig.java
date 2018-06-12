/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import java.io.Serializable;
import java.util.Objects;

/**
 * Metadata blok tanımları config modeli.
 * 
 * //TODO: Model modülüne mi gitmeli?
 * 
 * @author Hakan Uygun
 */
public class MetadataConfig implements Serializable{
    
    private String name;
    private String type;
    private String cnd;
    private boolean selectable = true;
    private String mimeType = "*";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCnd() {
        return cnd;
    }

    public void setCnd(String cnd) {
        this.cnd = cnd;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.name);
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
        final MetadataConfig other = (MetadataConfig) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "MetadataConfig{" + "name=" + name + ", type=" + type + ", cnd=" + cnd + ", selectable=" + selectable + ", mimeType=" + mimeType + '}';
    }
    
    
}
