package com.ozguryazilim.raf.forms.model;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author oyas
 */
public class MultiSelectField extends TextField {

    private List<String> values = new ArrayList<>();
    private String group;
    private String[] selecedValues;

    public String getGroup() {
        return group;
    }

    public String[] getSelecedValues() {
        if (!Strings.isNullOrEmpty(this.getValue())) {
            this.selecedValues = this.getValue().split(",");
        }
        return selecedValues;
    }

    public List<String> getValues() {
        return values;
    }

    public void setGroup(String group) {
        this.group = group;
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

    public void setValues(List<String> values) {
        this.values = values;
    }

    @Override
    public String getType() {
        return "MultiSelect";
    }

}
