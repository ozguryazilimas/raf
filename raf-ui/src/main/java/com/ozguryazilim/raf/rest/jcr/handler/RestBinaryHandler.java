package com.ozguryazilim.raf.rest.jcr.handler;

import org.modeshape.common.util.StringUtil;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * Class which handles incoming requests related to {@link Binary binary values}
 *
 * @author Horia Chiorean
 */
public final class RestBinaryHandler extends AbstractHandler {

    /**
     * The default content disposition prefix, used when serving binary content.
     */
    public static final String DEFAULT_CONTENT_DISPOSITION_PREFIX = "attachment;filename=";
    private static final String DEFAULT_MIME_TYPE = MediaType.APPLICATION_OCTET_STREAM;

    /**
     * Returns a binary {@link Property} for the given repository, workspace and path.
     *
     * @param request a non-null {@link HttpServletRequest} request
     * @param binaryAbsPath a non-null {@link String} representing the absolute path to a binary property.
     * @return the {@link Property} instance which is located at the given path. If such a property is not located, an exception
     *         is thrown.
     * @throws RepositoryException if any JCR related operation fails, including the case when the path to the property isn't valid.
     */
    public Property getBinaryProperty( HttpServletRequest request,
                                       String binaryAbsPath ) throws RepositoryException {
        Session session = getSession(request);
        return session.getProperty(binaryAbsPath);
    }

    /**
     * Returns a default Content-Disposition {@link String} for a given binary property.
     *
     * @param binaryProperty a non-null {@link Property}
     * @return a non-null String which represents a valid Content-Disposition.
     * @throws RepositoryException if any JCR related operation involving the binary property fail.
     */
    public String getDefaultContentDisposition( Property binaryProperty ) throws RepositoryException {
        Node parentNode = getParentNode(binaryProperty);
        String parentName = parentNode.getName();
        if (StringUtil.isBlank(parentName)) {
            parentName = "binary";
        }
        return DEFAULT_CONTENT_DISPOSITION_PREFIX + parentName;
    }

    /**
     * Returns the default mime-type of a given binary property.
     *
     * @param binaryProperty a non-null {@link Property}
     * @return a non-null String which represents the mime-type of the binary property.
     * @throws RepositoryException if any JCR related operation involving the binary property fail.
     */
    public String getDefaultMimeType( Property binaryProperty ) throws RepositoryException {
        try {
            Binary binary = binaryProperty.getBinary();
            return binary instanceof org.modeshape.jcr.api.Binary ? ((org.modeshape.jcr.api.Binary)binary)
                    .getMimeType() : DEFAULT_MIME_TYPE;
        } catch (IOException e) {
            logger.warn("Cannot determine default mime-type", e);
            return DEFAULT_MIME_TYPE;
        }
    }

}
