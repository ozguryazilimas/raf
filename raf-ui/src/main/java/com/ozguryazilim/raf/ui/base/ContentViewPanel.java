package com.ozguryazilim.raf.ui.base;

/**
 *
 * @author oyas
 */
public interface ContentViewPanel {
    
    String getName();
    boolean getSupportCollection();
    String getIcon();
    String getTitle();
    String getActionIcon();
    String getActionTitle();
    boolean getSupportPaging();
    boolean getSupportBreadcrumb();
    String getViewId();
    String getPermission();
    boolean isSupportMetadata();
}
