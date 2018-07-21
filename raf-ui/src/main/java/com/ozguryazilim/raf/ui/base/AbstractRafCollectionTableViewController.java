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
public abstract class AbstractRafCollectionTableViewController extends AbstractRafCollectionViewController{
    
    @Override
    public String getViewId() {
        //FIXME: bunu Pages üzerinden almak lazım.
        return "/fragments/collectionTableView.xhtml";
    }
}
