package com.ozguryazilim.raf.jcr;

import com.ozguryazilim.raf.converter.PDFPreviewConverter;
import org.imgscalr.Scalr;
import org.modeshape.common.util.CheckArg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FilePreviewHelper {

    private static final Logger LOG = LoggerFactory.getLogger(FilePreviewHelper.class);

    public static boolean generatePreview(Property inputProperty, Node outputNode, String mimeType) throws RepositoryException {
        switch (mimeType) {
            case "application/pdf":
                return generatePDFPreview(inputProperty, outputNode);
            case "image/png":
                return generateImagePreview(inputProperty, outputNode);
            default:
                return false;
        }
    }

    private static boolean generateImagePreview(Property inputProperty, Node outputNode) {
        try {
            Binary binaryValue = getBinary(inputProperty);
            InputStream isOrj = binaryValue.getStream();
            BufferedImage bufferedImageOrj = ImageIO.read(isOrj);
            isOrj.close();
            if (bufferedImageOrj.getWidth() > 480) {
                BufferedImage scaledImg = Scalr.resize(bufferedImageOrj, Scalr.Method.BALANCED, 480, 320, Scalr.OP_ANTIALIAS);

                ByteArrayOutputStream os = new ByteArrayOutputStream();

                ImageIO.write(scaledImg, "png", os);

                createPreviewNode(os, outputNode, "image/png");

                os.close();
            }

            LOG.debug("preview generating success..");
        } catch (Exception e) {
            LOG.warn("Preview cannot generated", e);
            return false;
        }

        //Yapılan değişiklikler kayıt edilsin.
        return true;
    }

    private static boolean generatePDFPreview(Property inputProperty, Node outputNode) {
        try {
            Binary binaryValue = getBinary(inputProperty);
            //Node sequencedNode = getPdfMetadataNode(outputNode);

            PDFPreviewConverter converter = new PDFPreviewConverter();

            ByteArrayOutputStream os = new ByteArrayOutputStream();

            String mimeType = inputProperty.getParent().getProperty("jcr:mimeType").getString();

            converter.convert(binaryValue.getStream(), mimeType, os);

            createPreviewNode(os, outputNode, "application/pdf");

            os.close();
        } catch (Exception e) {
            LOG.warn("Preview cannot generated", e);
            return false;
        }

        //Yapılan değişiklikler kayıt edilsin.
        return true;

    }

    private static Binary getBinary(Property inputProperty) throws RepositoryException {
        Binary binaryValue = inputProperty.getBinary();
        CheckArg.isNotNull(binaryValue, "binary");
        return binaryValue;
    }

    private static void createPreviewNode(ByteArrayOutputStream os, Node outputNode, String mimeType) throws RepositoryException, IOException {
        Node previewNode = getPreviewNode(outputNode);
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        Binary preview = outputNode.getSession().getValueFactory().createBinary(is);
        is.close();
        previewNode.setProperty("jcr:mimeType", mimeType);
        previewNode.setProperty("jcr:data", preview);

    }

    private static Node getPreviewNode(Node outputNode) throws RepositoryException {
        if (outputNode.isNew()) {
            outputNode.setPrimaryType("raf:preview");
            return outputNode;
        }

        return outputNode.addNode("raf:preview", "raf:preview");
    }
}
