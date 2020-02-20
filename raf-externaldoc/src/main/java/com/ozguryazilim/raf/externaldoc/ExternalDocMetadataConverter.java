package com.ozguryazilim.raf.externaldoc;

import com.ozguryazilim.raf.MetadataConverter;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.models.RafMetadata;
import java.util.Calendar;
import java.util.Date;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

/**
 * externalDoc:metadata node type'ı RafMetadata'ya dönüştürür.
 *
 * @author Hakan Uygun
 */
public class ExternalDocMetadataConverter implements MetadataConverter {

    @Override
    public RafMetadata nodeToModel(Node node) throws RafException {
        try {
            RafMetadata result = new RafMetadata();

            if (!node.isNodeType("externalDoc:metadata")) {
                //FIXME: Daha düzgün bir hata çevir.
                throw new RafException("[RAF-0032] Raf Node is not Metatata node");
            }

            result.setNodeId(node.getIdentifier());
            result.setType(node.getPrimaryNodeType().getName());

            PropertyIterator it = node.getProperties();
            while (it.hasNext()) {
                Property p = it.nextProperty();
                if (!p.getName().startsWith("jcr:")) {
                    //FIXME: Veri tipi kontrolü ile doğru bir şekilde aktarılmalı.
                    result.getAttributes().put(p.getName(), p.getValue().getString());
                }

            }

            return result;
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0030] Raf Metatata Convert Error", ex);
        }
    }

    @Override
    public void modelToNode(RafMetadata data, Node node) throws RafException {

        try {
            node.setProperty("externalDoc:documentId", (String) data.getAttributes().get("externalDoc:documentId"));
            node.setProperty("externalDoc:documentCreator", (String) data.getAttributes().get("externalDoc:documentCreator"));
            Calendar c = Calendar.getInstance();
            c.setTime((Date) data.getAttributes().get("externalDoc:documentCreateDate"));
            node.setProperty("externalDoc:documentCreateDate", c);
            node.setProperty("externalDoc:documentFolder", (String) data.getAttributes().get("externalDoc:documentFolder"));
            node.setProperty("externalDoc:documentFormat", (String) data.getAttributes().get("externalDoc:documentFormat"));
            node.setProperty("externalDoc:documentName", (String) data.getAttributes().get("externalDoc:documentName"));
            node.setProperty("externalDoc:documentParentFolder", (String) data.getAttributes().get("externalDoc:documentParentFolder"));
            node.setProperty("externalDoc:documentType", (String) data.getAttributes().get("externalDoc:documentType"));
            node.setProperty("externalDoc:documentStatus", (String) data.getAttributes().get("externalDoc:documentStatus"));

            //Calendar tipini nasıl setliyoruz ki?
            //node.setProperty("invoice:date", ((Date) data.getAttributes().get("invoice:date")));
        } catch (VersionException ex) {
            throw new RafException("[RAF-0030] Raf Metatata Convert Error", ex);
        } catch (LockException ex) {
            throw new RafException("[RAF-0030] Raf Metatata Convert Error", ex);
        } catch (ConstraintViolationException ex) {
            throw new RafException("[RAF-0030] Raf Metatata Convert Error", ex);
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0030] Raf Metatata Convert Error", ex);
        }

    }

}
