package com.ozguryazilim.raf.rest.jcr.handler;

import com.ozguryazilim.raf.rest.jcr.RestHelper;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.modeshape.common.annotation.Immutable;
import org.modeshape.common.util.Base64;
import org.modeshape.jcr.api.JcrConstants;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.VersionManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Resource handler that implements REST methods for items.
 */
@Immutable
public abstract class ItemHandler extends AbstractHandler {

    protected static final String CHILD_NODE_HOLDER = "children";

    private static final String PRIMARY_TYPE_PROPERTY = JcrConstants.JCR_PRIMARY_TYPE;
    private static final String MIXIN_TYPES_PROPERTY = JcrConstants.JCR_MIXIN_TYPES;
    private static final String PROPERTIES_HOLDER = "properties";

    protected boolean hasChildren( JSONObject jsonNode ) {
        return jsonNode.has(CHILD_NODE_HOLDER);
    }

    private String nameOf( Node node ) throws RepositoryException {
        int index = node.getIndex();
        String childName = node.getName();
        return index == 1 ? childName : childName + "[" + index + "]";
    }

    protected JSONObject getProperties( JSONObject jsonNode ) throws JSONException {
        return jsonNode.has(PROPERTIES_HOLDER) ? jsonNode.getJSONObject(PROPERTIES_HOLDER) : new JSONObject();
    }

    private Value createBinaryValue( String base64EncodedValue,
                                     ValueFactory valueFactory ) throws RepositoryException {
        InputStream stream = null;
        try {
            byte[] binaryValue = Base64.decode(base64EncodedValue);

            stream = new ByteArrayInputStream(binaryValue);
            Binary binary = valueFactory.createBinary(stream);
            return valueFactory.createValue(binary);
        } catch (IOException ioe) {
            throw new RepositoryException(ioe);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                logger.debug("Error while closing binary stream", e);
            }
        }
    }

    protected static class JSONChild {
        private final String name;
        private final JSONObject body;
        private final int snsIdx;

        protected JSONChild( String name, JSONObject body, int snsIdx ) {
            this.name = name;
            this.body = body;
            this.snsIdx = snsIdx;
        }

        public String getName() {
            return name;
        }

        public String getNameWithSNS() {
            return snsIdx > 1 ? name + "[" + snsIdx + "]" : name;
        }

        public JSONObject getBody() {
            return body;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("JSONChild{");
            sb.append("name='").append(getNameWithSNS()).append('\'');
            sb.append(", body=").append(body);
            sb.append('}');
            return sb.toString();
        }
    }

    protected static class VersionableChanges {
        private final Set<String> changedVersionableNodes = new HashSet<String>();
        private final Session session;
        private final VersionManager versionManager;

        protected VersionableChanges( Session session ) throws RepositoryException {
            this.session = session;
            assert this.session != null;
            this.versionManager = session.getWorkspace().getVersionManager();
        }

        public void checkout( Node node ) throws RepositoryException {
            boolean versionable = node.isNodeType("mix:versionable");
            if (versionable) {
                String path = node.getPath();
                versionManager.checkout(path);
                this.changedVersionableNodes.add(path);
            }
        }

        public void checkin() throws RepositoryException {
            if (this.changedVersionableNodes.isEmpty()) {
                return;
            }
            session.save();
            RepositoryException first = null;
            for (String path : this.changedVersionableNodes) {
                try {
                    if (versionManager.isCheckedOut(path)) {
                        versionManager.checkin(path);
                    }
                } catch (RepositoryException e) {
                    if (first == null) {
                        first = e;
                    }
                }
            }
            if (first != null) {
                throw first;
            }
        }

        public void abort() throws RepositoryException {
            if (this.changedVersionableNodes.isEmpty()) {
                return;
            }
            // Throw out all the changes ...
            session.refresh(false);
            RepositoryException first = null;
            for (String path : this.changedVersionableNodes) {
                try {
                    if (versionManager.isCheckedOut(path)) {
                        versionManager.checkin(path);
                    }
                } catch (RepositoryException e) {
                    if (first == null) {
                        first = e;
                    }
                }
            }
            if (first != null) {
                throw first;
            }
        }
    }

}
