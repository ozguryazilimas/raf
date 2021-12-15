package com.ozguryazilim.raf.jcr;

import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PdfUtils {

    private PdfUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * PDF döküman üzerinde verilen range parametresine göre dökümandan yeni bir parça oluşturur.
     * Genellikle preview'lar için kullanılır.
     *
     * @param document
     * @param range    -> example: "1-2" or "1-5"
     * @throws IOException
     */
    public static PDDocument createPdfDocPartial(PDDocument document, String range) throws IOException {
        List<Integer> rangeArr = new ArrayList<>();
        try {
            rangeArr.addAll(Arrays.stream(range.split("-")).map(Integer::parseInt).collect(Collectors.toList()));
        } catch (Exception ex) {
            rangeArr.add(1);
            rangeArr.add(2);
        }
        Splitter splitter = new Splitter();
        splitter.setStartPage(rangeArr.get(0));
        splitter.setEndPage(rangeArr.get(1));
        splitter.setSplitAtPage(rangeArr.get(1));
        List<PDDocument> pages = splitter.split(document);
        return pages.get(0);
    }

}