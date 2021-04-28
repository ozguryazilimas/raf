package com.ozguryazilim.raf.forms.model;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author oyas
 */
public class MultiPersonSelectionField extends TextField {

    private List<String> values = new ArrayList<>();
    private String[] selecedValues;

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
        return "MultiPersonSelection";
    }

    public String[] getSelecedValues() {
        if (!Strings.isNullOrEmpty(this.getValue())) {
            this.selecedValues = this.getValue().split(",");
        }
        return selecedValues;
    }

    public void setSelecedValues(String[] selecedValues) {
        this.selecedValues = selecedValues;
        String newValue = "";
        if (selecedValues != null) {
            for (String selecedValue : selecedValues) {
                newValue += selecedValue.replaceAll(",", "").concat(",");
            }
            this.setValue(newValue);
        }
    }

}
