package com.ozguryazilim.raf.rest.jcr.model;

/**
 * An interface which should be implemented by objects that provide a custom string representation.
 * 
 * @author Horia Chiorean (hchiorea@redhat.com)
 */
public interface Stringable {
    /**
     * Returns the string representation of this object.
     * 
     * @return a {@code non-null} string representation
     */
    public String asString();
}
