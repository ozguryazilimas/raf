package com.ozguryazilim.raf.record;

import com.ozguryazilim.raf.record.model.RafRecordDocumentType;
import com.ozguryazilim.raf.record.model.RafRecordProcess;
import com.ozguryazilim.raf.record.model.RafRecordType;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RecordType tanımlarını içeren xml dosyalarını parse eder.
 * 
 * @author Hakan Uygun
 */
public class RecordTypeXmlParser {
    
    private static final Logger LOG = LoggerFactory.getLogger(RecordTypeXmlParser.class);
    
    public synchronized static List<RafRecordType> parse(InputStream is) {
        List<RafRecordType> recordTypes = new ArrayList<>();
        
        if (is == null) {
            LOG.warn("InputStream cannot be null");
            return recordTypes;
        }

        try {

            SAXReader reader = new SAXReader();
            Document document = reader.read(is);

            Element root = document.getRootElement();

            if (!"recordTypes".equals(root.getName())) {
                LOG.warn("This is not a recordTypes xml : {}", document);
                return recordTypes;
            }

            List<Element> elements = root.elements("recordType");

            for (Element e : elements) {
                parseRecordType(e, recordTypes);
            }

            return recordTypes;

        } catch (DocumentException ex) {
            LOG.error("Record Types cannot read", ex);
            return recordTypes;
        }

    }

    private static void parseRecordType(Element e, List<RafRecordType> recordTypes) {
        
        RafRecordType recordType = new RafRecordType();
        
        recordType.setName(e.attributeValue("name"));
        recordType.setTitle(e.attributeValue("title"));
        recordType.setMetadata(e.attributeValue("metadata"));
        recordType.setForm(e.attributeValue("form"));
        
        Element docTypes = e.element("documentTypes");
        List<Element> elements = docTypes.elements("documentType");
        for (Element ed : elements) {
            parseDocumentType(ed, recordType.getDocumentTypes());
        }
        
        Element processes = e.element("processes");
        elements = processes.elements("process");
        for (Element ed : elements) {
            parseProcess(ed, recordType.getProcesses());
        }
        
        recordTypes.add(recordType);
    }

    private static void parseDocumentType(Element e, List<RafRecordDocumentType> documentTypes) {
        RafRecordDocumentType documentType = new RafRecordDocumentType();
                
        documentType.setName(e.attributeValue("name"));
        documentType.setTitle(e.attributeValue("title"));
        documentType.setMetadata(e.attributeValue("metadata"));
        documentType.setForm(e.attributeValue("form"));
        
        documentTypes.add(documentType);
    }

    private static void parseProcess(Element ed, List<RafRecordProcess> processes) {
        RafRecordProcess process = new RafRecordProcess();
        
        process.setName(ed.attributeValue("name"));
        process.setTitle(ed.attributeValue("title"));
        
        processes.add(process);
    }
}
