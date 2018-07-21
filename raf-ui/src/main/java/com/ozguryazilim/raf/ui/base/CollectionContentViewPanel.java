/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.ui.base;

import com.google.common.base.Strings;
import org.apache.deltaspike.core.util.ProxyUtils;

/**
 *
 * @author oyas
 */
public interface CollectionContentViewPanel extends ContentViewPanel {

    @Override
    default String getName() {
        return getClass().getSimpleName();
    }

    @Override
    default boolean getSupportCollection() {
        return true;
    }

    @Override
    default String getActionIcon(){
        ContentPanel a = getAnnotation();
        
        if( !Strings.isNullOrEmpty(a.actionIcon())){
            return a.actionIcon();
        }
        
        return "panel.action.icon." + getClass().getSimpleName();
    }

    @Override
    default String getActionTitle() {
        ContentPanel a = getAnnotation();

        if (!Strings.isNullOrEmpty(a.actionTitle())) {
            return a.actionTitle();
        }

        return "panel.action.title." + getClass().getSimpleName();
    }

    @Override
    default boolean getSupportPaging() {
        return false;
    }

    /**
     *
     * @return
     */
    @Override
    default boolean getSupportBreadcrumb() {
        return true;
    }

    @Override
    default String getPermission() {
        return "";
    }

    @Override
    default boolean isSupportMetadata() {
        return false;
    }

    default ContentPanel getAnnotation() {
        return (ContentPanel) ProxyUtils.getUnproxiedClass(this.getClass()).getAnnotation(ContentPanel.class);
    }
}
