package com.ozguryazilim.raf.forms.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author oyas
 */
public class PersonSelectionField extends TextField {

    private List<String> values = new ArrayList<>();
    private String role;

    public String getRole() {
        return role;
    }

    public List<String> getValues() {
        return values;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    @Override
    public String getType() {
        return "PersonSelection";
    }
}
