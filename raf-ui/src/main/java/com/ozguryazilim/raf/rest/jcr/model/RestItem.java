package com.ozguryazilim.raf.rest.jcr.model;

/**
 * A REST representation of a {@link javax.jcr.Item}
 *
 * @author Horia Chiorean (hchiorea@redhat.com)
 */
public abstract class RestItem implements JSONAble {

    protected final String url;
    protected final String parentUrl;
    protected final String name;

    protected RestItem( String name,
                        String url,
                        String parentUrl ) {
        this.name = name;
        this.url = url;
        this.parentUrl = parentUrl;
    }
}
