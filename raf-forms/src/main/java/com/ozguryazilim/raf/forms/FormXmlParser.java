/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.forms;

import com.ozguryazilim.raf.forms.builders.AbstractFieldBuilder;
import com.ozguryazilim.raf.forms.model.AbstractField;
import com.ozguryazilim.raf.forms.model.Form;
import java.io.InputStream;
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
 * <form id="unique" formKey="METADAT|TASK|PROCESS NAME" version="" >
 * <field id="" uitype="" datatype="" valueKey="" readonly="" required="" label="" placeholder="" defautlValue="" />
 * </form>
 *
 * @author Hakan Uygun
 */
public class FormXmlParser {

    private static final Logger LOG = LoggerFactory.getLogger(FormXmlParser.class);

    public static Form parse(InputStream is) {

        if (is == null) {
            LOG.warn("InputStream cannot be null");
            return null;
        }

        try {

            SAXReader reader = new SAXReader();
            Document document = reader.read(is);
            
            Element root = document.getRootElement();
            
            if( !"form".equals(root.getName())){
                LOG.warn("This is not a form.xml : {}", document);
                return null;
            }
            
            String formKey = root.attributeValue("formKey");
            String id = root.attributeValue("id");
            String version = root.attributeValue("version");
            String title = root.attributeValue("title");
            
            FormBuilder formBuilder = FormBuilder.createForm(formKey).withId(id).withVersion(version).withTitle(title);
            
            
            List<Element> elements = root.elements("field");
            for (Element e : elements) {
                
                String uitype = e.attributeValue("uitype");
                
                AbstractFieldBuilder fb = FieldTypeRegistery.getFieldBuilder(uitype);
                
                Map<String,String> attributes = new HashMap<>();                
                //Parse and build Fields
                for (Iterator it = e.attributes().iterator(); it.hasNext();) {
                    Attribute a = (Attribute) it.next();
                    attributes.put(a.getName(), a.getValue());
                    
                }
                
                LOG.debug("attributes {}", attributes );
                AbstractField f = fb.build(attributes);
                
                formBuilder.addField(f);
            }
            
            
            return formBuilder.build();
            
        }  catch (DocumentException | InstantiationException | IllegalAccessException ex) {
            LOG.error("form cannot read", ex);
            return null;
        }

    }
}
