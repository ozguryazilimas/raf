package com.ozguryazilim.raf.preview;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.jcr.Binary;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import org.imgscalr.Scalr;
import org.modeshape.common.util.CheckArg;
import org.modeshape.jcr.api.nodetype.NodeTypeManager;
import org.modeshape.jcr.api.sequencer.Sequencer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Raf'a yüklenen görsellerden preview amacıyla scale edilmiş sürümler üretir.
 *
 * @author Hakan Uygun
 */
public class ImagePreviewSequencer extends Sequencer {

    private static final Logger LOG = LoggerFactory.getLogger(ImagePreviewSequencer.class);

    @Override
    public void initialize(NamespaceRegistry registry, NodeTypeManager nodeTypeManager) throws RepositoryException, IOException {
        super.registerNodeTypes("/preview.cnd", nodeTypeManager, true);
    }

    @Override
    public boolean execute(Property inputProperty, Node outputNode, Context context) throws Exception {

        try {
            Binary binaryValue = inputProperty.getBinary();
            CheckArg.isNotNull(binaryValue, "binary");
            //Node sequencedNode = getPdfMetadataNode(outputNode);

            ByteArrayOutputStream os = new ByteArrayOutputStream();

            BufferedImage src = ImageIO.read(binaryValue.getStream());
            BufferedImage scaledImg = Scalr.resize(src, Scalr.Method.BALANCED, 480, 320, Scalr.OP_ANTIALIAS);
            ImageIO.write(scaledImg, "png", os);

            src.flush();
            scaledImg.flush();

            ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());

            Node previewNode = getPreviewNode(outputNode);

            Binary preview = outputNode.getSession().getValueFactory().createBinary(is);
            previewNode.setProperty("jcr:mimeType", "image/png");
            previewNode.setProperty("jcr:data", preview);
        } catch (Exception e) {
            LOG.warn("Preview cannot generated", e);
            return false;
        }

        //Yapılan değişiklikler kayıt edilsin.
        return true;
    }

    private Node getPreviewNode(Node outputNode) throws RepositoryException {
        if (outputNode.isNew()) {
            outputNode.setPrimaryType("raf:preview");
            return outputNode;
        }

        return outputNode.addNode("raf:preview", "raf:preview");
    }
}
