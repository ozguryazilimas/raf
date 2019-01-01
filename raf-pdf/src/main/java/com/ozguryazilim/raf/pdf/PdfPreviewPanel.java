package com.ozguryazilim.raf.pdf;

import com.ozguryazilim.raf.pdf.config.PdfPages;
import com.ozguryazilim.raf.ui.base.AbstractPreviewPanel;
import com.ozguryazilim.raf.ui.base.PreviewPanel;

/**
 * Tarayıcı yeteneği ile PDF preview'u sağlar.
 * 
 * Chrome, Firefox bu desteğe sahip.
 * 
 * FIXME: Performans açısından bişiler yapmak lazım. Preview için bütün belgeyi istemciye göndermek çok anlamlı değil. ilk 2-3 sayfa nasıl olur?
 * 
 * @author Hakan Uygun
 */
@PreviewPanel(view = PdfPages.PdfPreviewPanel.class, mimeType = "application/pdf")
public class PdfPreviewPanel extends AbstractPreviewPanel{

}
