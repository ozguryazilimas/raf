package com.ozguryazilim.raf.ui.base;

/**
 *
 * @author oyas
 */
public abstract class AbstractRafCollectionIconViewController extends AbstractRafCollectionViewController{
    
    @Override
    public String getViewId() {
        //FIXME: bunu Pages üzerinden almak lazım.
        return "/fragments/collectionIconView.xhtml";
    }
}
