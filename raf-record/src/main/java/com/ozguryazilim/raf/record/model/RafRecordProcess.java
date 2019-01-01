package com.ozguryazilim.raf.record.model;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author oyas
 */
public class RafRecordProcess implements Serializable{
    
    private String name;
    private String title;

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

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + Objects.hashCode(this.name);
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
        final RafRecordProcess other = (RafRecordProcess) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RafRecordProcess{" + "name=" + name + ", title=" + title + '}';
    }
    
    
}
