package com.ozguryazilim.raf.rest.jcr.handler;

import com.ozguryazilim.raf.jcr.ModeShapeRepositoryFactory;
import com.ozguryazilim.raf.rest.jcr.RestHelper;
import static com.ozguryazilim.raf.rest.jcr.RestHelper.BINARY_METHOD_NAME;
import static com.ozguryazilim.raf.rest.jcr.RestHelper.ITEMS_METHOD_NAME;
import static com.ozguryazilim.raf.rest.jcr.RestHelper.URL_ENCODER;
import com.ozguryazilim.raf.rest.jcr.model.RestException;
import com.ozguryazilim.raf.rest.jcr.model.RestItem;
import com.ozguryazilim.raf.rest.jcr.model.RestNode;
import com.ozguryazilim.raf.rest.jcr.model.RestProperty;
import org.modeshape.common.util.StringUtil;
import org.modeshape.jcr.api.JcrConstants;
import org.modeshape.jcr.api.RepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for the different rest handler implementations, to which the rest services delegate operations.
 */
public abstract class AbstractHandler {

    /**
     * Name to be used when the repository name is empty string as {@code "//"} is not a valid path.
     */
    public static final String EMPTY_REPOSITORY_NAME = "<default>";

    /**
     * The name of the custom property which will contain the node id
     */
    public static final String NODE_ID_CUSTOM_PROPERTY = RestNode.ID_FIELD_NAME;

    protected static final String BASE64_ENCODING_SUFFIX = "/base64/";

    /**
     * Name to be used when the workspace name is empty string as {@code "//"} is not a valid path.
     */
    protected static final String EMPTY_WORKSPACE_NAME = "<default>";

    /**
     * The holder of the active session. Since we're dealing with a service which operates on a request-response basis and
     * after each request the active session is closed, we don't need multiple active sessions (e.g. for each WS and repository)
     */
    private static final ThreadLocal<Session> ACTIVE_SESSION = new ThreadLocal<Session>();

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractHandler.class);

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Returns an active session for the given workspace name in the named repository.
     * 
     * @param request the servlet request; may not be null or unauthenticated
     * @return an active session with the given workspace in the named repository
     * @throws RepositoryException if any other error occurs
     */
    protected Session getSession( HttpServletRequest request ) throws RepositoryException {
        assert request != null;
        if (ACTIVE_SESSION.get() == null) {
            Session session = ModeShapeRepositoryFactory.getSession();
            ACTIVE_SESSION.set(session);
        }
        return ACTIVE_SESSION.get();
    }

    /**
     * Cleans up any resources related to {@link AbstractHandler#ACTIVE_SESSION}
     */
    public static void cleanupActiveSession() {
        Session session = AbstractHandler.ACTIVE_SESSION.get();
        if (session != null) {
            try {
                AbstractHandler.ACTIVE_SESSION.remove();
                session.logout();
                LOGGER.debug("Logged out REST service session");
            } catch (Exception e) {
                LOGGER.warn("Error while trying to logout REST service session", e);
            }
        }
    }

    private String workspaceNameFor( String rawWorkspaceName ) {
        String workspaceName = RestHelper.URL_ENCODER.decode(rawWorkspaceName);

        if (EMPTY_WORKSPACE_NAME.equals(workspaceName)) {
            workspaceName = "";
        }

        return workspaceName;
    }

    private String repositoryNameFor( String rawRepositoryName ) {
        String repositoryName = RestHelper.URL_ENCODER.decode(rawRepositoryName);

        if (EMPTY_REPOSITORY_NAME.equals(repositoryName)) {
            repositoryName = "";
        }

        return repositoryName;
    }

    protected List<String> restPropertyValues( Property property,
                                               String baseUrl,
                                               Session session ) throws RepositoryException {
        List<String> result = new ArrayList<String>();

        if (property.isMultiple()) {
            Value[] values = property.getValues();
            if (values == null || values.length == 0) {
                return null;
            }
            if (values.length == 1) {
                String value = valueToString(property.getPath(), values[0], baseUrl, session);
                if (value != null) {
                    result.add(value);
                }
            } else {
                for (Value value : values) {
                    if (value == null) {
                        continue;
                    }
                    String valueString = valueToString(property.getPath(), value, baseUrl, session);
                    if (valueString != null) {
                        result.add(valueString);
                    }
                }
            }
        } else {
            result.add(valueToString(property.getPath(), property.getValue(), baseUrl, session));
        }
        return result;
    }

    protected String valueToString( String absPropertyPath,
                                    Value value,
                                    String baseUrl,
                                    Session session ) {
        if (value == null) {
            return null;
        }
        try {
            switch (value.getType()) {
                case PropertyType.BINARY: {
                    assert baseUrl != null;
                    return restValueForBinary(absPropertyPath, baseUrl);
                }
                case PropertyType.REFERENCE:
                case PropertyType.WEAKREFERENCE:
                case org.modeshape.jcr.api.PropertyType.SIMPLE_REFERENCE: {
                    assert session != null;
                    return restValueForReference(value, baseUrl, session);
                }
                default: {
                    return value.getString();
                }
            }
        } catch (Exception e) {
            logger.error("Cannot create JSON string from value ", e);
            return null;
        }
    }

    private String restValueForReference( Value value,
                                          String baseUrl,
                                          Session session ) throws RepositoryException {
        String nodeId = value.getString();
        Node referredNode = session.getNodeByIdentifier(nodeId);
        if (referredNode != null) {
            return RestHelper.urlFrom(baseUrl, ITEMS_METHOD_NAME, encodedPath(referredNode.getPath()));
        }
        logger.warn("Cannot resolve reference with id: {0}", nodeId);
        return nodeId;
    }

    private String restValueForBinary( String absPropertyPath,
                                       String baseUrl ) {
        if (absPropertyPath == null) {
            logger.warn("Cannot generate rest representation of a binary value, because the property is unknown");
            return null;
        }
        return RestHelper.urlFrom(baseUrl, BINARY_METHOD_NAME, encodedPath(absPropertyPath));
    }

    protected Node getParentNode( Property property ) throws RepositoryException {
        Node parentNode = property.getParent();
        if (JcrConstants.JCR_CONTENT.equalsIgnoreCase(parentNode.getName()) && parentNode.getIndex() == 1) {
            parentNode = parentNode.getParent();
        }
        return parentNode;
    }

    protected Item itemAtPath( String path,
                               Session session ) throws RepositoryException {
        return isRootPath(path) ? session.getRootNode() : session.getItem(path);
    }

    protected Node nodeWithId( String id,
                               Session session ) throws RepositoryException {
        return session.getNodeByIdentifier(id);
    }

    protected boolean isRootPath( String path ) {
        return "/".equals(path) || "".equals(path);
    }

    protected RestItem createRestItem(HttpServletRequest request,
                                      int depth,
                                      Session session,
                                      Item item ) throws RepositoryException {
        String baseUrl = RestHelper.repositoryUrl(request);
        return item instanceof Node ? createRestNode(session, (Node)item, baseUrl, depth) : createRestProperty(session,
                                                                                                               (Property)item,
                                                                                                               baseUrl);
    }

    protected String parentPath( String path ) {
        int lastSlashInd = path.lastIndexOf('/');
        if (lastSlashInd == -1) {
            return "/";
        }
        String subPath = path.substring(0, lastSlashInd);
        return absPath(subPath);
    }

    protected String absPath( String pathString ) {
        return pathString.startsWith("/") ? pathString : "/" + pathString;
    }

    protected String nodeName( Node node ) throws RepositoryException {
        int index = node.getIndex();
        String name = node.getName();
        if (index != 1) name = name + "[" + index + "]";
        return name;
    }

    protected String encodedPath(String path) {
        if (StringUtil.isBlank(path)) {
            return "";
        }
        if (path.equalsIgnoreCase("/")) {
            return "/";
        }
        String[] parts = path.split("/");
        StringBuilder encodedPath = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (StringUtil.isBlank(part)) {
                continue;
            }
            encodedPath.append(URL_ENCODER.encode(part));
            if (i < parts.length - 1) {
                encodedPath.append("/");
            }
        }
        return encodedPath.toString();
    }

    private RestNode createRestNode( Session session,
                                     Node node,
                                     String baseUrl,
                                     int depth ) throws RepositoryException {
        String nodeUrl = RestHelper.urlFrom(baseUrl, ITEMS_METHOD_NAME, encodedPath(node.getPath()));
        boolean isRoot = node.getPath().equals("/");
        String parentUrl = isRoot ? RestHelper.urlFrom(baseUrl, ITEMS_METHOD_NAME, "..", "..") : RestHelper.urlFrom(baseUrl,
                                                                                                                    ITEMS_METHOD_NAME,
                                                                                                                    encodedPath(
                                                                                                                            node.getParent()
                                                                                                                                    .getPath()));
        RestNode restNode = new RestNode(nodeName(node), node.getIdentifier(), nodeUrl, parentUrl);

        // add the properties
        for (PropertyIterator propertyIterator = node.getProperties(); propertyIterator.hasNext();) {
            Property property = propertyIterator.nextProperty();
            restNode.addJcrProperty(createRestProperty(session, property, baseUrl));
        }

        // add the children
        for (NodeIterator nodeIterator = node.getNodes(); nodeIterator.hasNext();) {
            Node childNode = nodeIterator.nextNode();
            RestNode restChild = null;
            if (depth > 0) {
                restChild = createRestNode(session, childNode, baseUrl, depth - 1);
            } else if (depth < 0) {
                restChild = createRestNode(session, childNode, baseUrl, -1);
            } else {
                String childUrl = RestHelper.urlFrom(baseUrl, ITEMS_METHOD_NAME, encodedPath(childNode.getPath()));
                restChild = new RestNode(nodeName(childNode), childNode.getIdentifier(), childUrl, nodeUrl);
            }
            restNode.addChild(restChild);
        }
        return restNode;
    }

    private RestProperty createRestProperty(Session session,
                                            Property property,
                                            String baseUrl ) throws RepositoryException {
        List<String> values = restPropertyValues(property, baseUrl, session);
        String url = RestHelper.urlFrom(baseUrl, ITEMS_METHOD_NAME, encodedPath(property.getPath()));
        String parentUrl = RestHelper.urlFrom(baseUrl, ITEMS_METHOD_NAME, encodedPath(property.getParent().getPath()));
        boolean multiValued = property.isMultiple();
        return new RestProperty(property.getName(), url, parentUrl, values, multiValued);
    }

    protected Response exceptionResponse(String message) {
        return Response.status(Response.Status.BAD_REQUEST).entity(new RestException(message)).build();
    }
}
