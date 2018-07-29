/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.encoder;

/**
 * RafRepository içerisinde URL/Dosya isimlerinde özel karakterler temizlemek için Encoder/Decoder API
 * 
 * Burada ModeShape ile gelen URLEncoder ya da RafFileNameEncoder kullanılabilir.
 * 
 * Varsayılan olarak RafFileNameEncoder kullanılacak. Bunun için RafEncoderFactory sınıfı kullanılabilir.
 * 
 * @author Hakan Uygun
 */
public interface RafEncoder {
    String encode(String text);
    String decode(String encodedText);
}
