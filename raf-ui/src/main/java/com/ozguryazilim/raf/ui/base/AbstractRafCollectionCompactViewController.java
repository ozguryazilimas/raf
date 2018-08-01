/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.ui.base;

/**
 * Compact Sunum tipi için taban sınıf.
 * 
 * Bu sunum tipinde içerik aşağı + sağa doğru kolonlar halinde sıralanır.
 * 
 * @see Icon tipinde olanlar sola ve aşağı sıralar.
 * @see Detail tipinde olan ise tablo olarak sunum yapar
 * 
 * @author Hakan Uygun
 */
public class AbstractRafCollectionCompactViewController extends AbstractRafCollectionViewController{

    @Override
    public String getViewId() {
        //FIXME: bunu Pages üzerinden almak lazım.
        return "/fragments/collectionCompactView.xhtml";
    }
    
}
