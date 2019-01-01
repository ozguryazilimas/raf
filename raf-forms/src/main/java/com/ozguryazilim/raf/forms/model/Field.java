package com.ozguryazilim.raf.forms.model;

import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author oyas
 * @param <T>
 */
public interface Field<T extends Serializable> extends Serializable{
    
    String getType();
    
    String getId();
    void setId(String id);

    String getLabel();
    void setLabel(String label);

    String getPlaceholder();
    void setPlaceholder(String placeholder);

    String getPermission();
    void setPermission(String permission);

    Boolean getRequired();
    void setRequired(Boolean required);

    Boolean getReadonly();
    void setReadonly(Boolean readonly);

    T getDefaultValue();
    void setDefaultValue(T defaultValue);

    String getDataKey();
    void setDataKey(String dataKey);
    
    String getColumnClass();
    void setColumnClass(String columnClass);
    
    abstract Class<T> getValueClass();
    
    void setData( Map<String, Object> data );
    
    T getValue();
    void setValue( T value );
}
