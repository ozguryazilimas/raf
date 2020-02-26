package com.ozguryazilim.raf.forms.builders;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.ozguryazilim.raf.forms.model.AbstractField;
import com.ozguryazilim.raf.forms.model.MultiSelectField;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author oyas
 */
public class MultiSelectFieldBuilder extends AbstractFieldBuilder<MultiSelectField> {

    public MultiSelectField build(Map<String, String> attributes) {

        MultiSelectField result = new MultiSelectField();
        baseBuild(result, attributes);
        result.setDefaultValue(attributes.get("defaultValue"));
        //FIXME: Burada ek olarak valueListesi parse edilecek.

        String vals = attributes.get("values");
        if (!Strings.isNullOrEmpty(vals)) {
            result.setValues(Splitter.on(',').trimResults().omitEmptyStrings().splitToList(vals));
        }
        String group = attributes.get("group");
        if (!Strings.isNullOrEmpty(group)) {
            result.setGroup(group);
        }
        return result;
    }

    @Override
    public AbstractField build(MultiSelectField field) {
        MultiSelectField result = new MultiSelectField();
        baseBuild(field, result);
        result.setDefaultValue(field.getDefaultValue());

        result.setValues(new ArrayList<>(field.getValues()));
        result.setGroup(field.getGroup());

        return result;
    }
}
