package com.ozguryazilim.raf.ui.previewpanels;

import com.ozguryazilim.raf.config.PreviewPanelPages;
import com.ozguryazilim.raf.ui.base.AbstractPreviewPanel;
import com.ozguryazilim.raf.ui.base.PreviewPanel;

/**
 * Image tipinde içerikler için preview widget controller.
 * <p>
 * mimeType'ı image/* olan tipler için kullanılır. Tarayıcıların sunum yeteneği ile çalışır.
 *
 * @author Hakan Uygun
 */
@PreviewPanel(view = PreviewPanelPages.ImagePreviewPanel.class, mimeType = "image/")
public class ImagePreviewPanel extends AbstractPreviewPanel {
}
