package com.ozguryazilim.raf.ui.base;

import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.util.ProxyUtils;

import javax.inject.Inject;
import java.io.Serializable;

public class AbstractRafReaderPage implements Serializable {

    @Inject
    private ViewConfigResolver viewConfigResolver;

    public String getViewId() {
        return viewConfigResolver.getViewConfigDescriptor(getView()).getViewId();
    }

    public Class<? extends ViewConfig> getView() {
        return getAnnotation().view();
    }

    protected RafReader getAnnotation() {
        return (RafReader) ProxyUtils.getUnproxiedClass(this.getClass()).getAnnotation(RafReader.class);
    }

}