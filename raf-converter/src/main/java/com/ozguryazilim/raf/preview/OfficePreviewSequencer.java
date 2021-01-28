package com.ozguryazilim.raf.preview;

import com.ozguryazilim.raf.jcr.FilePreviewHelper;

import java.io.IOException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.modeshape.jcr.api.nodetype.NodeTypeManager;
import org.modeshape.jcr.api.sequencer.Sequencer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
public class OfficePreviewSequencer extends Sequencer {

    private static final Logger LOG = LoggerFactory.getLogger(OfficePreviewSequencer.class);

    @Override
    public void initialize(NamespaceRegistry registry, NodeTypeManager nodeTypeManager) throws RepositoryException, IOException {
        super.registerNodeTypes("/preview.cnd", nodeTypeManager, true);
    }

    @Override
    public boolean execute(Property inputProperty, Node outputNode, Context context) throws Exception {
        return FilePreviewHelper.generatePreview(inputProperty, outputNode,"application/pdf");
    }
}
