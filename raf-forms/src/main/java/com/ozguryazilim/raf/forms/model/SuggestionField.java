package com.ozguryazilim.raf.forms.model;

/**
 *
 * @author oyas
 */
public class SuggestionField extends TextField{
    
    private String group;
    private String key;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
    
    @Override
    public String getType() {
        return "Suggestion";
    }
}
