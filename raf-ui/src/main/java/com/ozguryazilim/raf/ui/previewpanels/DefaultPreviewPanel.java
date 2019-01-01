package com.ozguryazilim.raf.ui.previewpanels;

import com.ozguryazilim.raf.config.PreviewPanelPages;
import com.ozguryazilim.raf.ui.base.AbstractPreviewPanel;
import com.ozguryazilim.raf.ui.base.PreviewPanel;

/**
 * Gerçekten bir preview panel değil. Sadece preview desteklenmediğini söyler.
 * 
 * @author Hakan Uygun
 */
@PreviewPanel(view = PreviewPanelPages.DefaultPreviewPanel.class, mimeType = "default")
public class DefaultPreviewPanel extends AbstractPreviewPanel{
    
}
