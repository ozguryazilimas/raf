package com.ozguryazilim.raf.jcr;

import com.ozguryazilim.raf.converter.PDFPreviewConverter;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.imgscalr.Scalr;
import org.modeshape.common.util.CheckArg;
import org.modeshape.jcr.api.JcrConstants;
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
import java.util.List;

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


    public static boolean generateImagePreview(Property inputProperty, Node outputNode) {
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
            } else {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(bufferedImageOrj, "png", os);
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

    public static boolean generatePDFPreview(Property inputProperty, Node outputNode) {

        try {
            String mimeType = inputProperty.getParent().getProperty(JcrConstants.JCR_MIME_TYPE).getString();

            Binary binaryValue = getBinary(inputProperty);

            if (mimeType.equals("application/pdf")) {
                //application/pdf icin de 2 sayfalik preview ve thumbnail operasyonlari gerceklesecek.
                PDDocument document = PDDocument.load(binaryValue.getStream());
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                if ("IMAGE".equals(ConfigResolver.getPropertyValue("raf.pdf.preview.type", "PDF"))) {
                    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                        BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);
                        BufferedImage scaledImg = Scalr.resize(bim, Scalr.Method.BALANCED, 1024, 768, Scalr.OP_ANTIALIAS);
                        ImageIO.write(scaledImg, "png", os);
                        createPreviewNode(os, outputNode, "image/png");
//                        if ("true".equals(ConfigResolver.getPropertyValue("raf.thumbnail.pdf.when.uploaded", "false"))) {
//                            createThumbnailNode(os, outputNode, "image/png");
//                        }
                    }
                } else {
                    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                        Splitter splitter = new Splitter();
                        splitter.setStartPage(1);
                        splitter.setEndPage(2);
                        splitter.setSplitAtPage(2);
                        List<PDDocument> pages = splitter.split(document);
                        PDDocument pdfDocPartial = pages.get(0);
                        pdfDocPartial.save(os);
                        createPreviewNode(os, outputNode, "application/pdf");
                        if ("true".equals(ConfigResolver.getPropertyValue("raf.thumbnail.pdf.when.uploaded", "false"))) {
                            createThumbnailNode(os, outputNode, "image/png");
                        }
                    }
                }
                document.close();
                return true;
            }

            if (!PDFPreviewConverter.instance().isAcceptedMimeType(mimeType)) {
                return false;
            }

            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {

                PDFPreviewConverter.instance().convert(binaryValue.getStream(), mimeType, os);

                createPreviewNode(os, outputNode, "application/pdf");

                if("true".equals(ConfigResolver.getPropertyValue("raf.thumbnail.pdf.when.uploaded", "false"))) {
                    createThumbnailNode(os, outputNode, "image/png");
                }
            }
            
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

    private static void createThumbnailNode(ByteArrayOutputStream os, Node outputNode, String mimeType) throws RepositoryException, IOException {
        Node thumbnailNode = getThumbnailNode(outputNode);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
            Binary fileBinary = outputNode.getSession().getValueFactory().createBinary(is);
            PDDocument document = PDDocument.load(fileBinary.getStream());
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);
            BufferedImage scaledImg = Scalr.resize(bim, Scalr.Method.BALANCED, 480, 320, Scalr.OP_ANTIALIAS);
            ImageIO.write(scaledImg, "png", bos);
            Binary thumbnailBinary = outputNode.getSession().getValueFactory().createBinary(new ByteArrayInputStream(bos.toByteArray()));
            document.close();
            thumbnailNode.setProperty("jcr:mimeType", mimeType);
            thumbnailNode.setProperty("jcr:data", thumbnailBinary);
        }
    }

    private static void createPreviewNode(ByteArrayOutputStream os, Node outputNode, String mimeType) throws RepositoryException, IOException {
        Node previewNode = getPreviewNode(outputNode);
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        Binary preview = outputNode.getSession().getValueFactory().createBinary(is);
        is.close();
        previewNode.setProperty("jcr:mimeType", mimeType);
        previewNode.setProperty("jcr:data", preview);

    }

    private static Node getThumbnailNode(Node outputNode) throws RepositoryException {
        if (outputNode.isNew()) {
            outputNode.setPrimaryType("raf:thumbnail");
            return outputNode;
        } else if (outputNode.hasNode("raf:thumbnail")) {
            return outputNode.getNode("raf:thumbnail");
        }

        return outputNode.addNode("raf:thumbnail", "raf:thumbnail");
    }

    private static Node getPreviewNode(Node outputNode) throws RepositoryException {
        if (outputNode.isNew()) {
            outputNode.setPrimaryType("raf:preview");
            return outputNode;
        } else if (outputNode.hasNode("raf:preview")) {
            return outputNode.getNode("raf:preview");
        }

        return outputNode.addNode("raf:preview", "raf:preview");
    }
}
