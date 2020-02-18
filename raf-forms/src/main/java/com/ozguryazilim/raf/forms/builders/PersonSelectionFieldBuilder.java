package com.ozguryazilim.raf.forms.builders;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.ozguryazilim.raf.forms.model.AbstractField;
import com.ozguryazilim.raf.forms.model.PersonSelectionField;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author oyas
 */
public class PersonSelectionFieldBuilder extends AbstractFieldBuilder<PersonSelectionField> {

    public PersonSelectionField build(Map<String, String> attributes) {

        PersonSelectionField result = new PersonSelectionField();
        baseBuild(result, attributes);
        result.setDefaultValue(attributes.get("defaultValue"));
        //FIXME: Burada ek olarak valueListesi parse edilecek.

        String vals = attributes.get("values");
        if (!Strings.isNullOrEmpty(vals)) {
            result.setValues(Splitter.on(',').trimResults().omitEmptyStrings().splitToList(vals));
        }
        String role = attributes.get("role");
        if (!Strings.isNullOrEmpty(role)) {
            result.setRole(role);
        }
        return result;
    }

    @Override
    public AbstractField build(PersonSelectionField field) {
        PersonSelectionField result = new PersonSelectionField();
        baseBuild(field, result);
        result.setDefaultValue(field.getDefaultValue());

        result.setValues(new ArrayList<>(field.getValues()));
        result.setRole(field.getRole());

        return result;
    }
}
