package com.ozguryazilim.raf.forms.builders;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.ozguryazilim.raf.forms.model.AbstractField;
import com.ozguryazilim.raf.forms.model.MultiPersonSelectionField;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author oyas
 */
public class MultiPersonSelectionFieldBuilder extends AbstractFieldBuilder<MultiPersonSelectionField> {

    public MultiPersonSelectionField build(Map<String, String> attributes) {

        MultiPersonSelectionField result = new MultiPersonSelectionField();
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
    public AbstractField build(MultiPersonSelectionField field) {
        MultiPersonSelectionField result = new MultiPersonSelectionField();
        baseBuild(field, result);
        result.setDefaultValue(field.getDefaultValue());

        result.setValues(new ArrayList<>(field.getValues()));
        result.setRole(field.getRole());

        return result;
    }
}
