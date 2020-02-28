package com.ozguryazilim.raf;

import com.ozguryazilim.raf.auth.KJarResourceHandler;
import com.ozguryazilim.raf.auth.RafAsset;
import com.ozguryazilim.raf.jcr.ModeShapeRepositoryFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.PropertyDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KJar içerisinden cnd dosyası çıkarsa bunları da mevcut repository'e register
 * eder.
 *
 * @author Hakan Uygun
 */
@ApplicationScoped
public class KJarCNDHandler implements KJarResourceHandler {

    private static final Logger LOG = LoggerFactory.getLogger(KJarCNDHandler.class);

    @Override
    public boolean canHandle(String fileName) {
        return fileName.matches(".+.cnd$");
    }

    @Override
    public void handle(String kjarId, InputStream is) {
        try {
            Session session = ModeShapeRepositoryFactory.getSession();
            Workspace workspace = session.getWorkspace();
            org.modeshape.jcr.api.nodetype.NodeTypeManager nodeTypeMgr = (org.modeshape.jcr.api.nodetype.NodeTypeManager) workspace.getNodeTypeManager();
            NodeTypeIterator nodeTypeIterator = nodeTypeMgr.registerNodeTypes(is, true);
            while (nodeTypeIterator.hasNext()) {
                NodeType nodeType = nodeTypeIterator.nextNodeType();
                LOG.debug("{} node type registered.", nodeType.getName());
                for (PropertyDefinition propertyDefinition : nodeType.getPropertyDefinitions()) {
                    LOG.debug("{} node type property registered.", propertyDefinition.getName());
                }
            }
            session.logout();
            LOG.info("Node Types registered");
        } catch (RepositoryException | IOException ex) {
            LOG.error("Node Types can't register", ex);
        }

    }

    @Override
    public void undeploy(String kjarId) {
        //Register edilmiş CND undeploy edilemez!
    }

    @Override
    public List<RafAsset> getAssests(String kjarId) {
        //FIXME: bu bilgileri geri dönmek lazım mı?
        //Bu bilgiyi tutacak bir yapı yok!
        return Collections.emptyList();
    }

}
