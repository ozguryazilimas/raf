package com.ozguryazilim.raf.ocr.textextractor;

import com.ozguryazilim.raf.ocr.config.RafOcrConfig;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    public OcrTextExtractor() {
        LOG.debug("OcrTextExtractor");
    }

    @Override
    public boolean supportsMimeType(String mimeType) {
        LOG.debug("OCR Checking for mime type {}", mimeType);
        return mimeType.toLowerCase().startsWith("application/pdf") || mimeType.toLowerCase().startsWith("image/");
    }

    @Override
    public void extractFrom(Binary binary,
            TextExtractor.Output output,
            Context context) throws Exception {
        try {

            String extension = binary.getMimeType().contains("/") ? binary.getMimeType().split("/")[1] : "pdf";
            LOG.debug("OCR Text Extractiong {}", extension);
            String fileName = "temp".concat(UUID.randomUUID().toString()).concat(".").concat(extension);
            File tempFile = new File(fileName);
            byte[] buffer = new byte[8 * 1024];
            FileOutputStream fos = new FileOutputStream(tempFile);
            InputStream is = binary.getStream();
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            fos.close();
            String extractedText = RafOcrConfig.tesseract.doOCR(tempFile);
            output.recordText(extractedText);
            Files.delete(Paths.get(tempFile.getAbsolutePath()));
            LOG.debug("ocr extracted text : {}", extractedText);
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

}
