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
 * externalDoc:metadata node type'ı RafMetadata'ya dönüştürür.
 *
 * @author Hakan Uygun
 */
public class ExternalDocAnnotationMetadataConverter implements MetadataConverter {

    @Override
    public RafMetadata nodeToModel(Node node) throws RafException {
        try {
            RafMetadata result = new RafMetadata();

            if (!node.isNodeType("externalDocAnnotation:metadata")) {
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
            node.setProperty("externalDocAnnotation:annotationDateTime", (String) data.getAttributes().get("externalDocAnnotation:annotationDateTime"));
            node.setProperty("externalDocAnnotation:annotation", (String) data.getAttributes().get("externalDocAnnotation:annotation"));
            node.setProperty("externalDocAnnotation:annotationUser", (String) data.getAttributes().get("externalDocAnnotation:annotationUser"));
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
