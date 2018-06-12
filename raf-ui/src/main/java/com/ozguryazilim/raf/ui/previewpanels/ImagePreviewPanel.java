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
 * Image tipinde içerikler için preview widget controller.
 * 
 * mimeType'ı image/* olan tipler için kullanılır. Tarayıcıların sunum yeteneği ile çalışır.
 * 
 * FIXME: Kalite v.s. konusunda aslında bişiler yapılmalı. Belgenin tamamını clienta sırf preview için göndermek anlamlı değil.
 * 
 * @author Hakan Uygun
 */
@PreviewPanel(view = PreviewPanelPages.ImagePreviewPanel.class, mimeType = "image/")
public class ImagePreviewPanel extends AbstractPreviewPanel{

    
}
