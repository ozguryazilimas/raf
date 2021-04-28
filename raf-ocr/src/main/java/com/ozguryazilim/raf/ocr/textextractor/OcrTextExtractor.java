package com.ozguryazilim.raf.ocr.textextractor;

import com.ozguryazilim.raf.ocr.config.RafOcrConfig;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import net.sourceforge.tess4j.Tesseract;
import org.jgroups.util.UUID;
import org.modeshape.jcr.api.Binary;
import org.modeshape.jcr.api.text.TextExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
public class OcrTextExtractor extends TextExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(OcrTextExtractor.class);

    private final Set<String> excludedMediaTypes = new HashSet<>();
    private final Set<String> includedMediaTypes = new HashSet<>();

    private final Integer textLimit = 100000; // 100.000 char
    private final Integer fileLimit = 100000000;// 100 MB

    public OcrTextExtractor() {
        LOG.debug("OcrTextExtractor");
        excludedMediaTypes.add("application/x-archive");
        excludedMediaTypes.add("application/x-bzip");
        excludedMediaTypes.add("application/x-bzip2");
        excludedMediaTypes.add("application/zip");
        excludedMediaTypes.add("application/x-cpio");
        excludedMediaTypes.add("application/x-gtar");
        excludedMediaTypes.add("application/x-gzip");
        excludedMediaTypes.add("application/x-tar");
        excludedMediaTypes.add("video/*");
        excludedMediaTypes.add("audio/*");
        includedMediaTypes.add("image/*");
        includedMediaTypes.add("application/pdf");
    }

    @Override
    public void extractFrom(final Binary binary,
            final TextExtractor.Output output,
            Context context) throws Exception {
        try {

            String extension = binary.getMimeType().contains("/") ? binary.getMimeType().split("/")[1] : "pdf";
            LOG.debug("OCR Text Extracting {}", extension);
            final String fileName = RafOcrConfig.tempFilePath.concat(File.separator).concat("temp").concat(UUID.randomUUID().toString()).concat(".").concat(extension);
            processStream(binary, stream -> {
                try {
                    if (stream.available() <= fileLimit) {
                        File tempFile = new File(fileName);
                        LOG.debug("buffer creating.");
                        byte[] buffer = new byte[stream.available()];
                        LOG.debug("buffer creating.");
                        stream.read(buffer);
                        LOG.debug("stream reading.");
                        FileOutputStream fos = new FileOutputStream(tempFile);
                        LOG.debug("temp file writing.");
                        fos.write(buffer);
                        LOG.debug("FOS closing.");
                        fos.close();
                        LOG.debug("stream closing.");
                        stream.close();
                        LOG.debug("text extracting.");
                        Tesseract tesseract = new Tesseract();
                        tesseract.setDatapath(RafOcrConfig.tessDataPath);
                        tesseract.setLanguage(RafOcrConfig.language);
                        String extractedText = tesseract.doOCR(tempFile);
                        if (extractedText.length() > textLimit) {
                            extractedText = extractedText.substring(textLimit);
                        }
                        LOG.debug("text recording.");
                        output.recordText(extractedText.trim().replaceAll("\"", " ").replace("'", " "));
                        LOG.debug("text result {}.", extractedText.length() > 100 ? extractedText.substring(100) : extractedText);
                    }
                } catch (Exception e) {
                    LOG.error("Exception in ocr", e);
                } finally {
                    LOG.debug("temp file deleting.");
                    Files.delete(Paths.get(fileName));
                    LOG.debug("garbage collector.");
                    System.gc();
                }
                return null;
            });
        } catch (Exception e) {
            LOG.error("Exception in ocr", e);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OcrTextExtractor{");
        sb.append("excludedMediaTypes=").append(getExcludedMimeTypes());
        sb.append(", includedMediaTypes=").append(getIncludedMimeTypes());
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean supportsMimeType(String mimeType) {
        return mimeType.toLowerCase().contains("image/") || mimeType.toLowerCase().contains("application/pdf");
    }

    @Override
    protected Set<String> getExcludedMimeTypes() {
        return excludedMediaTypes;
    }

    @Override
    protected Set<String> getIncludedMimeTypes() {
        return includedMediaTypes;
    }

}
