package com.ozguryazilim.raf.forms;

import com.ozguryazilim.raf.forms.builders.AbstractFieldBuilder;
import com.ozguryazilim.raf.forms.builders.DateFieldBuilder;
import com.ozguryazilim.raf.forms.builders.MultiPersonSelectionFieldBuilder;
import com.ozguryazilim.raf.forms.builders.MultiSelectFieldBuilder;
import com.ozguryazilim.raf.forms.builders.PersonSelectionFieldBuilder;
import com.ozguryazilim.raf.forms.builders.RafDocumentFieldBuilder;
import com.ozguryazilim.raf.forms.builders.RafFolderFieldBuilder;
import com.ozguryazilim.raf.forms.builders.SelectionFieldBuilder;
import com.ozguryazilim.raf.forms.builders.SuggestionFieldBuilder;
import com.ozguryazilim.raf.forms.builders.TextFieldBuilder;
import com.ozguryazilim.raf.forms.builders.UserFieldBuilder;
import com.ozguryazilim.raf.forms.model.AbstractField;
import com.ozguryazilim.raf.forms.model.DateField;
import com.ozguryazilim.raf.forms.model.MultiPersonSelectionField;
import com.ozguryazilim.raf.forms.model.MultiSelectField;
import com.ozguryazilim.raf.forms.model.PersonSelectionField;
import com.ozguryazilim.raf.forms.model.RafDocumentField;
import com.ozguryazilim.raf.forms.model.RafFolderField;
import com.ozguryazilim.raf.forms.model.SelectionField;
import com.ozguryazilim.raf.forms.model.SuggestionField;
import com.ozguryazilim.raf.forms.model.TextField;
import com.ozguryazilim.raf.forms.model.UserField;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
public class FieldTypeRegistery {

    private static final Logger LOG = LoggerFactory.getLogger(FieldTypeRegistery.class);

    private static Map<String, Class<? extends AbstractField>> fieldTypeMap = new HashMap<>();
    private static Map<String, Class<? extends AbstractFieldBuilder>> fieldBuilderMap = new HashMap<>();

    public static void register(String typeName, Class<? extends AbstractField> clz, Class<? extends AbstractFieldBuilder> bclz) {
        fieldTypeMap.put(typeName, clz);
        fieldBuilderMap.put(typeName, bclz);
        LOG.info("Field Registered : {}", typeName);
    }

    public static Class<? extends AbstractField> getTypeClass(String typeName) {
        return fieldTypeMap.get(typeName);
    }

    public static AbstractField getField(String typeName) throws InstantiationException, IllegalAccessException {
        return getTypeClass(typeName).newInstance();
    }

    public static Class<? extends AbstractFieldBuilder> getBuilderClass(String typeName) {
        return fieldBuilderMap.get(typeName);
    }

    public static AbstractFieldBuilder getFieldBuilder(String typeName) throws InstantiationException, IllegalAccessException {
        Class<? extends AbstractFieldBuilder> clz = getBuilderClass(typeName);
        if (clz == null) {
            LOG.error("Type not registered : {}", typeName);
            return null;
        }
        return clz.newInstance();
    }

    static {
        FieldTypeRegistery.register("Text", TextField.class, TextFieldBuilder.class);
        FieldTypeRegistery.register("Selection", SelectionField.class, SelectionFieldBuilder.class);
        FieldTypeRegistery.register("PersonSelection", PersonSelectionField.class, PersonSelectionFieldBuilder.class);
        FieldTypeRegistery.register("Suggestion", SuggestionField.class, SuggestionFieldBuilder.class);
        FieldTypeRegistery.register("Date", DateField.class, DateFieldBuilder.class);
        FieldTypeRegistery.register("User", UserField.class, UserFieldBuilder.class);
        FieldTypeRegistery.register("RafFolder", RafFolderField.class, RafFolderFieldBuilder.class);
        FieldTypeRegistery.register("RafDocument", RafDocumentField.class, RafDocumentFieldBuilder.class);
        FieldTypeRegistery.register("MultiSelect", MultiSelectField.class, MultiSelectFieldBuilder.class);
        FieldTypeRegistery.register("MultiPersonSelection", MultiPersonSelectionField.class, MultiPersonSelectionFieldBuilder.class);
    }
}
