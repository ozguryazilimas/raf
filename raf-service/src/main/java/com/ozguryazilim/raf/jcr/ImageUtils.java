package com.ozguryazilim.raf.jcr;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ImageUtils {

    public ImageUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static BufferedImage createPreviewImageFromPDF(PDFRenderer pdfRenderer) throws IOException {
        BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);
        List<Integer> pixels = getPreviewPixels();
        return Scalr.resize(bim, Scalr.Method.BALANCED, pixels.get(0), pixels.get(1), Scalr.OP_ANTIALIAS);
    }

    public static BufferedImage createPreviewImageFromInputStream(InputStream is) throws IOException {
        BufferedImage bim = ImageIO.read(is);
        List<Integer> pixels = getPreviewPixels();
        return Scalr.resize(bim, Scalr.Method.BALANCED, pixels.get(0), pixels.get(1), Scalr.OP_ANTIALIAS);
    }

    public static BufferedImage createThumbnailImageFromPDF(PDFRenderer pdfRenderer) throws IOException {
        BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);
        List<Integer> pixels = getThumbnailPixels();
        return Scalr.resize(bim, Scalr.Method.BALANCED, pixels.get(0), pixels.get(1), Scalr.OP_ANTIALIAS);
    }

    public static BufferedImage createThumbnailImageFromInputStream(InputStream is) throws IOException {
        BufferedImage bim = ImageIO.read(is);
        List<Integer> pixels = getThumbnailPixels();
        return Scalr.resize(bim, Scalr.Method.BALANCED, pixels.get(0), pixels.get(1), Scalr.OP_ANTIALIAS);
    }

    public static BufferedImage resizePreviewImage(BufferedImage image) {
        List<Integer> pixels = getPreviewPixels();
        return Scalr.resize(image, Scalr.Method.BALANCED, pixels.get(0), pixels.get(1), Scalr.OP_ANTIALIAS);
    }

    private static List<Integer> getPreviewPixels() {
        String pixels = ConfigResolver.getPropertyValue("raf.image.preview.pixels", "2480x3578");
        return getPixelList(pixels, 2480, 3578);
    }

    private static List<Integer> getThumbnailPixels() {
        String pixels = ConfigResolver.getPropertyValue("raf.image.thumbnail.pixels", "1280x720");
        return getPixelList(pixels, 1280, 720);
    }

    /**
     * "1280x720" gibi bir string ifadeyi ayırarak integer listesi olarak döner.
     *
     * @param pixels
     * @param defaultWidth
     * @param defaultHeight
     * @return
     */
    private static List<Integer> getPixelList(String pixels, Integer defaultWidth, Integer defaultHeight) {
        List<Integer> pixelArr = new ArrayList<>();
        try {
            pixelArr.addAll(Arrays.stream(pixels.split("x")).map(Integer::parseInt).collect(Collectors.toList()));
        } catch (Exception ex) {
            pixelArr.add(defaultWidth);
            pixelArr.add(defaultHeight);
        }
        return pixelArr;
    }

}