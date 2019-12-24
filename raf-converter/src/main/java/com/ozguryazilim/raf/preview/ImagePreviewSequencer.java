package com.ozguryazilim.raf.preview;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
            LOG.debug("{} preview file is creating..", inputProperty.getName());
            Binary binaryValue = inputProperty.getBinary();
            CheckArg.isNotNull(binaryValue, "binary");
            InputStream isOrj = binaryValue.getStream();
            BufferedImage bufferedImageOrj = ImageIO.read(isOrj);
            isOrj.close();
            if (bufferedImageOrj.getWidth() > 480) {
                BufferedImage scaledImg = Scalr.resize(bufferedImageOrj, Scalr.Method.BALANCED, 480, 320, Scalr.OP_ANTIALIAS);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(scaledImg, "png", os);
                byte[] bytes = os.toByteArray();
                os.close();
                ByteArrayInputStream isScaled = new ByteArrayInputStream(bytes);

                Binary preview = outputNode.getSession().getValueFactory().createBinary(isScaled);
                isScaled.close();
                Node previewNode = getPreviewNode(outputNode);
                previewNode.setProperty("jcr:mimeType", "image/png");
                previewNode.setProperty("jcr:data", preview);
            }

            LOG.debug("preview generating success..");
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
