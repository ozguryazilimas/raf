/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf;

/**
 *
 * @author oyas
 */
public interface ContentPanel {
    
    String getTitle();
    
    String getIcon();
    
    String getFragment();
    
    Boolean supportPaging();

    String getCommandIcon();
    
    String getCommandTitle();
}