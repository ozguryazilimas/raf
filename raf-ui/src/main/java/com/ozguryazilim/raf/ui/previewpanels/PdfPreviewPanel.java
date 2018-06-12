/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.ui.previewpanels;

import com.ozguryazilim.raf.config.PreviewPanelPages;
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
@PreviewPanel(view = PreviewPanelPages.PdfPreviewPanel.class, mimeType = "application/pdf")
public class PdfPreviewPanel extends AbstractPreviewPanel{

}
