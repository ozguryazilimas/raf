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
