package com.ozguryazilim.raf.ui.readers;

import com.ozguryazilim.raf.config.ReaderPages;
import com.ozguryazilim.raf.ui.base.AbstractRafReaderPage;
import com.ozguryazilim.raf.ui.base.RafReader;

@RafReader(view = ReaderPages.PdfReaderPage.class, mimeType = "application\\/(csv|pdf|msword|(vnd\\.(ms-|openxmlformats-|oasis).*))")
public class PdfReaderPage extends AbstractRafReaderPage {
}