/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

import java.io.Serializable;

/**
 *
 * @author oyas
 */
public interface RafDocumentPreview extends Serializable{
    
    /**
     * Preview için kullanılacak olan JSF fragment ismi
     * @return 
     */
    String getFragment();
    
}
