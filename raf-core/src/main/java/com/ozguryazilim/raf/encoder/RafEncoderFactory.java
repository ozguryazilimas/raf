/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.encoder;

/**
 * Farklı Encoder implemenstasyonları için factory.
 * 
 * FIXME: burada alternatif URLEncoder da döndürülebilmeli.
 * 
 * @author Hakan Uygun
 */
public class RafEncoderFactory {
    
    /**
     * Geriye varsayılan encoder implementasyonu döndürürlür.
     * 
     * FIXME: Varsayılan değer configden alınmalı
     * @return 
     */
    public static RafEncoder getEncoder(){
        return new RafFileNameEncoder();
    }
    
    
}
