package com.ozguryazilim.raf.encoder;

/**
 * RafRepository içerisinde URL/Dosya isimlerinde özel karakterler temizlemek için Encoder/Decoder API
 * 
 * Burada ModeShape ile gelen URLEncoder ya da FileNameEncoder kullanılabilir.
 * 
 * Varsayılan olarak FileNameEncoder kullanılacak. Bunun için RafEncoderFactory sınıfı kullanılabilir.
 * 
 * @author Hakan Uygun
 */
public interface RafEncoder {
    String encode(String text);
    String decode(String encodedText);
}
