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
    NeedClipboard,

    /**
     * Aksiyon durumu ve buna ikon değişimi
     * Değiştirilebilir Durumlar implementasyona N kadar tanımlanabilir
     * Örn: Favorites -> Eğer dizin halihazırda favorilere eklenmiş ise silme işlemi yapacak, Aksi durumda Ekleme.
     * Bu aksiyonun implementasyonunda durum değişimlerine göre yeni ikonları döndürecek.
     */
    ChangeableStateIcon
}
