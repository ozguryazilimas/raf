package com.ozguryazilim.raf.ui.base;

/**
 *
 * @author oyas
 */
public enum ActionCapability {
    /**
     * Collection Tipi View'larda gösterilebilir
     */
    CollectionViews,
    /**
     * Detay sunum tipi viewlerde gösterilebilir
     */
    DetailViews,
    /**
     * Çoklu seçim destekler
     */
    MultiSelection,
    /**
     * Ajax destekler.
     */
    Ajax,
    /**
     * Confirmation destekler
     */
    Confirmation,
    
    /**
     * Mutlaka seçim yapılmış olmalı
     */
    NeedSelection,
    
    /**
     * Clipboard içinde veri olmalı
     */
    NeedClipboard
}
