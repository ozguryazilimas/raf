/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
