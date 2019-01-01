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
