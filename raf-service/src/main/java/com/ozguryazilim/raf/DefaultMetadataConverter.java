package com.ozguryazilim.raf;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.models.RafMetadata;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
public class DefaultMetadataConverter implements MetadataConverter {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultMetadataConverter.class);
    
    @Override
    public RafMetadata nodeToModel(Node node) throws RafException {

        try {
            RafMetadata result = new RafMetadata();
            //node.isNodeType("*:metadata")

            result.setNodeId(node.getIdentifier());
            result.setType(node.getPrimaryNodeType().getName());

            PropertyIterator it = node.getProperties();
            while (it.hasNext()) {
                Property p = it.nextProperty();
                if (!p.getName().startsWith("jcr:")) {
                    //FIXME: Veri tipi kontrolü ile doğru bir şekilde aktarılmalı.
                    switch( p.getValue().getType() ){
                        case PropertyType.STRING : 
                            result.getAttributes().put(p.getName(), p.getString());
                            break;
                        case PropertyType.BOOLEAN : 
                            result.getAttributes().put(p.getName(), p.getBoolean());
                            break;
                        case PropertyType.LONG : 
                            result.getAttributes().put(p.getName(), p.getLong());
                            break;
                        case PropertyType.DATE : 
                            result.getAttributes().put(p.getName(), p.getDate().getTime());
                            break;
                        case PropertyType.DECIMAL : 
                            result.getAttributes().put(p.getName(), p.getDecimal());
                            break;
                        default:
                            LOG.warn("Cannot convert {}:{}", p.getName(), p.getString());
                    }
                }
            }

            return result;
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0030] Raf Metatata Convert Error",ex);
        }
    }

    @Override
    public void modelToNode(RafMetadata data, Node node) throws RafException {
        try {
            for (Map.Entry<String, Object> e : data.getAttributes().entrySet()) {
                if (e.getValue() instanceof String) {
                    if( !Strings.isNullOrEmpty((String) e.getValue())){
                        node.setProperty(e.getKey(), (String) e.getValue());
                    }
                } else if (e.getValue() instanceof Long) {
                    node.setProperty(e.getKey(), (long) e.getValue());
                } else if (e.getValue() instanceof Boolean ) {
                    node.setProperty(e.getKey(), (boolean) e.getValue());
                } else if (e.getValue() instanceof BigDecimal ) {
                    node.setProperty(e.getKey(), (BigDecimal) e.getValue());
                } else if (e.getValue() instanceof Date ) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime((Date) e.getValue());
                    node.setProperty(e.getKey(), cal);
                } else {
                    LOG.warn("Cannot convert {}:{}", e.getKey(), e.getValue());
                }

            }
        } catch (VersionException ex) {
            throw new RafException("[RAF-0030] Raf Metatata Convert Error", ex);
        } catch (LockException | ConstraintViolationException ex) {
            throw new RafException("[RAF-0030] Raf Metatata Convert Error", ex);
        } catch (RepositoryException ex) {
            throw new RafException("[RAF-0030] Raf Metatata Convert Error", ex);
        }

    }

}
