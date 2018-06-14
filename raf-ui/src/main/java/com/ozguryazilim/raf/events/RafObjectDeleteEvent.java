/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
