package com.ozguryazilim.raf.forms;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.forms.builders.AbstractFieldBuilder;
import com.ozguryazilim.raf.forms.model.AbstractField;
import com.ozguryazilim.raf.forms.model.Form;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Form bilgilerini XML parse ederek oluşturur.
 *
 * <forms>
 *  <form id="unique" formKey="METADAT|TASK|PROCESS NAME" version="" >
 *      <field id="" uitype="" datatype="" valueKey="" readonly="" required="" label="" placeholder="" defautlValue="" />
 *  </form>
 * </forms>
 *
 * @author Hakan Uygun
 */
public class FormXmlParser {

    private static final Logger LOG = LoggerFactory.getLogger(FormXmlParser.class);

    public static List<Form> parse(InputStream is) {

        List<Form> forms = new ArrayList<>();
        
        if (is == null) {
            LOG.warn("InputStream cannot be null");
            return forms;
        }

        try {

            SAXReader reader = new SAXReader();
            Document document = reader.read(is);

            Element root = document.getRootElement();

            if (!"forms".equals(root.getName())) {
                LOG.warn("This is not a form.xml : {}", document);
                return forms;
            }

            List<Element> elements = root.elements("form");

            for (Element e : elements) {
                parseForm(e, forms);
            }

            return forms;

        } catch (DocumentException ex) {
            LOG.error("form cannot read", ex);
            return null;
        }

    }

    protected static void parseForm(Element ef, List<Form> forms) {
        try {
            String formKey = ef.attributeValue("formKey");
            String id = ef.attributeValue("id");
            String version = ef.attributeValue("version");
            String title = ef.attributeValue("title");
            String base = ef.attributeValue("base");

            if( Strings.isNullOrEmpty(id) ){
                id = formKey;
            }
            
            FormBuilder formBuilder = FormBuilder.createForm(formKey).withId(id).withVersion(version).withTitle(title).fromBase(base);

            List<Element> elements = ef.elements("field");
            for (Element e : elements) {

                String uitype = e.attributeValue("type");

                AbstractFieldBuilder fb = FieldTypeRegistery.getFieldBuilder(uitype);
                if( fb != null ){

                Map<String, String> attributes = new HashMap<>();
                //Parse and build Fields
                for (Iterator it = e.attributes().iterator(); it.hasNext();) {
                    Attribute a = (Attribute) it.next();
                    attributes.put(a.getName(), a.getValue());

                }

                LOG.debug("attributes {}", attributes);
                AbstractField f = fb.build(attributes);

                formBuilder.addField(f);
                } else {
                    LOG.error("Field type {} not registered", uitype);
                }
            }

            forms.add(formBuilder.build());
        } catch (InstantiationException | IllegalAccessException ex) {
            LOG.error("form cannot read", ex);
            return;
        }
    }
}
