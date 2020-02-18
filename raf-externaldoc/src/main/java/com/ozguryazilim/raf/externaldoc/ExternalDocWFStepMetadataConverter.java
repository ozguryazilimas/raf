package com.ozguryazilim.raf.externaldoc;

import com.ozguryazilim.raf.MetadataConverter;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.models.RafMetadata;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

/**
 * externalDocWFStep:metadata node type'ı RafMetadata'ya dönüştürür.
 *
 * @author Hakan Uygun
 */
public class ExternalDocWFStepMetadataConverter implements MetadataConverter {

    @Override
    public RafMetadata nodeToModel(Node node) throws RafException {
        try {
            RafMetadata result = new RafMetadata();

            if (!node.isNodeType("externalDocWFStep:metadata")) {
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
            node.setProperty("externalDocWFStep:documentWFId", (String) data.getAttributes().get("externalDocWFStep:documentWFId"));
            node.setProperty("externalDocWFStep:startedDateTime", (String) data.getAttributes().get("externalDocWFStep:startedDateTime"));
            node.setProperty("externalDocWFStep:starter", (String) data.getAttributes().get("externalDocWFStep:starter"));
            node.setProperty("externalDocWFStep:state", (String) data.getAttributes().get("externalDocWFStep:state"));
            node.setProperty("externalDocWFStep:completeDateTime", (String) data.getAttributes().get("externalDocWFStep:completeDateTime"));
            node.setProperty("externalDocWFStep:completer", (String) data.getAttributes().get("externalDocWFStep:completer"));
            node.setProperty("externalDocWFStep:stepName", (String) data.getAttributes().get("externalDocWFStep:stepName"));
            node.setProperty("externalDocWFStep:comment", (String) data.getAttributes().get("externalDocWFStep:comment"));
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
