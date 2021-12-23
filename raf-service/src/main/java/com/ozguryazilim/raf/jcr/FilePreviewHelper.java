package com.ozguryazilim.raf.jcr;

import com.ozguryazilim.raf.converter.PdfConverter;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.jodconverter.core.office.OfficeException;
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

public class FilePreviewHelper {

    private static final Logger LOG = LoggerFactory.getLogger(FilePreviewHelper.class);

    private static final String MIMETYPE_IMAGE = "image/png";
    private static final String MIMETYPE_PDF = "application/pdf";

    private static final String IMAGE = "IMAGE";

    private static final String PROP_DATA = "jcr:data";
    private static final String PROP_MIMETYPE = "jcr:mimeType";

    private static final String NODE_PREVIEW = "raf:preview";
    private static final String NODE_THUMBNAIL = "raf:thumbnail";

    public static boolean generatePreview(Property inputProperty, Node outputNode, String mimeType) {
        switch (mimeType) {
            case MIMETYPE_PDF:
                return generatePDFPreview(inputProperty, outputNode);
            case MIMETYPE_IMAGE:
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
                BufferedImage scaledImg = ImageUtils.resizePreviewImage(bufferedImageOrj);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(scaledImg, "png", os);
                createPreviewNode(os, outputNode, MIMETYPE_IMAGE);
                os.close();
            } else {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(bufferedImageOrj, "png", os);
                createPreviewNode(os, outputNode, MIMETYPE_IMAGE);
                os.close();
            }

            LOG.debug("Image preview generating success. NodeID: {}, Path: {}", outputNode.getIdentifier(), outputNode.getPath());
        } catch (Exception e) {
            try {
                LOG.warn("Image preview cannot generated. NodeID: " + outputNode.getIdentifier() + ", Path: " + outputNode.getPath(), e);
            } catch (RepositoryException ex) {
                LOG.warn("Image preview cannot generated. NodeID not found.", e);
            }
            return false;
        }

        //Yapılan değişiklikler kayıt edilsin.
        return true;
    }

    public static boolean generatePDFPreview(Property inputProperty, Node outputNode) {
        try {
            String mimeType = inputProperty.getParent().getProperty(JcrConstants.JCR_MIME_TYPE).getString();
            Binary binaryValue = getBinary(inputProperty);
            // pdf dosyaları için preview ve thumbnail operasyonları
            if (mimeType.equals(MIMETYPE_PDF)) {
                return generatePreviewAndThumbnailForPDFDocuments(outputNode, binaryValue);
            }
            // mimeType pdf preview converter'ın çalışması için uygun mu?
            if (!PdfConverter.instance().isAcceptedMimeType(mimeType)) {
                return false;
            }
            // diğer tüm document tipindeki dosyalar(word,excel vb.) için preview operasyonları
            generatePreviewForOtherDocuments(outputNode, mimeType, binaryValue);
            // diğer tüm document tipindeki dosyalar için thumbnail operasyonları
            generateThumbnailForOtherDocuments(outputNode);
            LOG.debug("Document preview generating success. NodeID: {}, Path: {}", outputNode.getIdentifier(), outputNode.getPath());
        } catch (Exception e) {
            try {
                LOG.warn("Document preview cannot generated. NodeID: " + outputNode.getIdentifier() + ", Path: " + outputNode.getPath(), e);
            } catch (RepositoryException ex) {
                LOG.warn("Document preview cannot generated. NodeID not found.", e);
            }
            return false;
        }
        //Yapılan değişiklikler kayıt edilsin.
        return true;
    }

    /**
     * PDF dosyaları için preview ve thumbnail oluşturur.
     *
     * @param outputNode
     * @param binaryValue
     * @return
     */
    private static boolean generatePreviewAndThumbnailForPDFDocuments(Node outputNode, Binary binaryValue) {
        try {
            // application/pdf icin de 2 sayfalik preview ve thumbnail operasyonlari gerceklesecek.
            PDDocument document = PDDocument.load(binaryValue.getStream());
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            // PDF'in ilk sayfası image olarak preview node'unda saklansın mı?
            if (IMAGE.equals(ConfigResolver.getPropertyValue("raf.pdf.preview.type", IMAGE))) {
                try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                    BufferedImage img = ImageUtils.createPreviewImageFromPDF(pdfRenderer);
                    ImageIO.write(img, "png", os);
                    createPreviewNode(os, outputNode, MIMETYPE_IMAGE);
                }
            } else { // Yoksa verilen aralık kadar pdf şeklinde preview node'unda saklanacak.
                try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                    String range = ConfigResolver.getPropertyValue("raf.preview.office.range", "1-2");
                    try (PDDocument pdfDocPartial = PdfUtils.createPdfDocPartial(document, range)) {
                        pdfDocPartial.save(os);
                        createPreviewNode(os, outputNode, MIMETYPE_PDF);
                    } catch (IOException ex) {
                        LOG.warn("Pdf preview cannot generated. NodeID: " + outputNode.getIdentifier() + " Path: " + outputNode.getPath(), ex);
                    }
                }
            }

            // dosya ilk yüklendiği anda thumbnail node oluşsun ve image olarak saklansın mı?
            // eğer bu ayar kapalı ise gallery view görünümünde gözüken thumbnail'ler her seferinde baştan oluşur.
            if (storeThumbnailWhenDocumentUploaded()) {
                try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                    BufferedImage img = ImageUtils.createThumbnailImageFromPDF(pdfRenderer);
                    ImageIO.write(img, "png", os);
                    createThumbnailNode(os, outputNode);
                }
            } else {
                removeThumbnailNode(outputNode);
            }
            LOG.debug("Pdf preview generating success. NodeID: {}, Path: {}", outputNode.getIdentifier(), outputNode.getPath());
            document.close();
            return true;
        } catch (Exception e) {
            try {
                LOG.warn("Pdf preview cannot generated. NodeID: " + outputNode.getIdentifier() + " Path: " + outputNode.getPath(), e);
            } catch (RepositoryException ex) {
                LOG.warn("Pdf preview cannot generated. NodeID not found.", e);
            }
            return false;
        }
    }

    /**
     * Diğer döküman tipleri için preview oluşturur.
     *
     * @param outputNode
     * @param mimeType
     * @param binaryValue
     * @throws IOException
     * @throws OfficeException
     * @throws RepositoryException
     */
    private static void generatePreviewForOtherDocuments(Node outputNode, String mimeType, Binary binaryValue) throws IOException, OfficeException, RepositoryException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {

            PdfConverter.instance().convertPreview(binaryValue.getStream(), mimeType, os);

            if (IMAGE.equals(ConfigResolver.getPropertyValue("raf.pdf.preview.type", IMAGE))) {
                ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
                Binary preview = outputNode.getSession().getValueFactory().createBinary(is);
                is.close();
                try (ByteArrayOutputStream os2 = new ByteArrayOutputStream()) {
                    PDDocument document = PDDocument.load(preview.getStream());
                    PDFRenderer pdfRenderer = new PDFRenderer(document);
                    BufferedImage img = ImageUtils.createPreviewImageFromPDF(pdfRenderer);
                    ImageIO.write(img, "png", os2);
                    createPreviewNode(os2, outputNode, MIMETYPE_IMAGE);
                    document.close();
                }
            } else {
                createPreviewNode(os, outputNode, MIMETYPE_PDF);
            }

        }
    }

    /**
     * Diğer döküman tipleri thumbnail oluşturur.
     *
     * @param outputNode
     * @throws RepositoryException
     * @throws IOException
     */
    private static void generateThumbnailForOtherDocuments(Node outputNode) throws RepositoryException, IOException {
        if (storeThumbnailWhenDocumentUploaded()) {

            if (isPreviewTypePDF(outputNode)) {
                InputStream is = outputNode.getNode(NODE_PREVIEW).getProperty(PROP_DATA).getBinary().getStream();
                PDDocument document = PDDocument.load(is);
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                    BufferedImage img = ImageUtils.createThumbnailImageFromPDF(pdfRenderer);
                    ImageIO.write(img, "png", os);
                    createThumbnailNode(os, outputNode);
                }
                document.close();
            } else if (isPreviewTypeImage(outputNode)) {
                try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                    InputStream is = outputNode.getNode(NODE_PREVIEW).getProperty(PROP_DATA).getBinary().getStream();
                    BufferedImage img = ImageUtils.createThumbnailImageFromInputStream(is);
                    ImageIO.write(img, "png", os);
                    is.close();
                    createThumbnailNode(os, outputNode);
                }
            }

        } else {
            removeThumbnailNode(outputNode);
        }
    }

    private static Binary getBinary(Property inputProperty) throws RepositoryException {
        Binary binaryValue = inputProperty.getBinary();
        CheckArg.isNotNull(binaryValue, "binary");
        return binaryValue;
    }

    private static void createThumbnailNode(ByteArrayOutputStream os, Node outputNode) throws RepositoryException, IOException {
        Node thumbnailNode = getThumbnailNode(outputNode);
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        Binary thumbnail = outputNode.getSession().getValueFactory().createBinary(is);
        is.close();
        thumbnailNode.setProperty(PROP_MIMETYPE, MIMETYPE_IMAGE);
        thumbnailNode.setProperty(PROP_DATA, thumbnail);
    }

    private static void removeThumbnailNode(Node outputNode) {
        try {
            if (outputNode.hasNode(NODE_THUMBNAIL)) {
                outputNode.getNode(NODE_THUMBNAIL).remove();
            }
        } catch (Exception e) {
            try {
                LOG.warn("Thumbnail node cannot removed. NodeID: " + outputNode.getIdentifier() + " Path: " + outputNode.getPath(), e);
            } catch (RepositoryException ex) {
                LOG.warn("Pdf preview cannot generated. NodeID not found.", e);
            }
        }
    }

    private static void createPreviewNode(ByteArrayOutputStream os, Node outputNode, String mimeType) throws RepositoryException, IOException {
        Node previewNode = getPreviewNode(outputNode);
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        Binary preview = outputNode.getSession().getValueFactory().createBinary(is);
        is.close();
        previewNode.setProperty(PROP_MIMETYPE, mimeType);
        previewNode.setProperty(PROP_DATA, preview);
    }

    private static Node getThumbnailNode(Node outputNode) throws RepositoryException {
        if (outputNode.isNew()) {
            outputNode.setPrimaryType(NODE_THUMBNAIL);
            return outputNode;
        } else if (outputNode.hasNode(NODE_THUMBNAIL)) {
            return outputNode.getNode(NODE_THUMBNAIL);
        }

        return outputNode.addNode(NODE_THUMBNAIL, NODE_THUMBNAIL);
    }

    private static Node getPreviewNode(Node outputNode) throws RepositoryException {
        if (outputNode.isNew()) {
            outputNode.setPrimaryType(NODE_PREVIEW);
            return outputNode;
        } else if (outputNode.hasNode(NODE_PREVIEW)) {
            return outputNode.getNode(NODE_PREVIEW);
        }

        return outputNode.addNode(NODE_PREVIEW, NODE_PREVIEW);
    }

    private static boolean storeThumbnailWhenDocumentUploaded() {
        return "true".equals(ConfigResolver.getPropertyValue("raf.thumbnail.store.when.uploaded", "true"));
    }

    private static boolean isPreviewTypePDF(Node n) throws RepositoryException {
        if (n.hasNode(NODE_PREVIEW)) {
            Node pNode = n.getNode(NODE_PREVIEW);
            if (pNode.hasProperty(PROP_MIMETYPE)) {
                return pNode.getProperty(PROP_MIMETYPE).getString().equals(MIMETYPE_PDF);
            }
        }
        return false;
    }

    private static boolean isPreviewTypeImage(Node n) throws RepositoryException {
        if (n.hasNode(NODE_PREVIEW)) {
            Node pNode = n.getNode(NODE_PREVIEW);
            if (pNode.hasProperty(PROP_MIMETYPE)) {
                return pNode.getProperty(PROP_MIMETYPE).getString().equals(MIMETYPE_IMAGE);
            }
        }
        return false;
    }

}