package com.ozguryazilim.raf.emaildoc;

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
 * emailDoc:metadata node type'ı RafMetadata'ya dönüştürür.
 *
 * @author Hakan Uygun
 */
public class EmailDocMetadataConverter implements MetadataConverter {

    @Override
    public RafMetadata nodeToModel(Node node) throws RafException {
        try {
            RafMetadata result = new RafMetadata();

            if (!node.isNodeType("emailDoc:metadata")) {
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
            node.setProperty("emailDoc:messageId", (String) data.getAttributes().get("emailDoc:messageId"));
            node.setProperty("emailDoc:from", (String) data.getAttributes().get("emailDoc:from"));
            Calendar c = Calendar.getInstance();
            c.setTime((Date) data.getAttributes().get("emailDoc:date"));
            node.setProperty("emailDoc:date", c);
            node.setProperty("emailDoc:subject", (String) data.getAttributes().get("emailDoc:subject"));
            node.setProperty("emailDoc:toList", (String) data.getAttributes().get("emailDoc:toList"));
            node.setProperty("emailDoc:ccList", (String) data.getAttributes().get("emailDoc:ccList"));
            node.setProperty("emailDoc:bccList", (String) data.getAttributes().get("emailDoc:bccList"));
            node.setProperty("emailDoc:references", (String) data.getAttributes().get("emailDoc:references"));
            node.setProperty("emailDoc:relatedReferenceId", (String) data.getAttributes().get("emailDoc:relatedReferenceId"));

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
