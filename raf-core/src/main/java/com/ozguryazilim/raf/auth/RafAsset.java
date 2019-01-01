package com.ozguryazilim.raf.auth;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author oyas
 */
public class RafAsset implements Serializable{
    
    private String id;
    private String name;
    private String type;

    public RafAsset() {
    }

    public RafAsset(String id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.id);
        hash = 67 * hash + Objects.hashCode(this.type);
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
        final RafAsset other = (RafAsset) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RafAsset{" + "id=" + id + ", name=" + name + ", type=" + type + '}';
    }
    
    
    
}
