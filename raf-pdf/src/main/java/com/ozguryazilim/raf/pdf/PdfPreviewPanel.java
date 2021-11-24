package com.ozguryazilim.raf.pdf;

import com.ozguryazilim.raf.pdf.config.PdfPages;
import com.ozguryazilim.raf.ui.base.AbstractPreviewPanel;
import com.ozguryazilim.raf.ui.base.PreviewPanel;

/**
 * Tarayıcı yeteneği ile PDF preview'u sağlar.
 * 
 * Chrome, Firefox bu desteğe sahip.
 * 
 * 
 * @author Hakan Uygun
 */
@PreviewPanel(view = PdfPages.PdfPreviewPanel.class, mimeType = "application/pdf")
public class PdfPreviewPanel extends AbstractPreviewPanel{

}
