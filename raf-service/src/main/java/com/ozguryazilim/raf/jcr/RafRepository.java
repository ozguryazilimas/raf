/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.jcr;

import com.ozguryazilim.raf.RafException;

/**
 * RAF Repository için temel API.
 * 
 * CDI ile birlikte kullanmak için bir Producer kullanılarak doğru IMPL'den bir tane üretmek gerekir.
 * 
 * @author Hakan Uygun
 */
public interface RafRepository {
 
    /**
     * Repository init olması için çağrılır.
     * 
     * @throws RafException 
     */
    void start() throws RafException;
    
    /**
     * Repository kapatılması için çağrılır.
     * 
     * @throws RafException 
     */
    void stop() throws RafException;
    
}
