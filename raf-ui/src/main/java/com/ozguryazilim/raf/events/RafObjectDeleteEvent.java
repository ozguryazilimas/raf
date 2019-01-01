package com.ozguryazilim.raf.events;

import com.ozguryazilim.raf.models.RafObject;

/**
 *
 * @author oyas
 */
public class RafObjectDeleteEvent {
    
    private RafObject payload;

    public RafObjectDeleteEvent(RafObject payload) {
        this.payload = payload;
    }

    public RafObject getPayload() {
        return payload;
    }
    
    
}
