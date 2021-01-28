package com.ozguryazilim.raf.encoder;

import java.util.regex.Pattern;


/**
 * Raf name için encode decode işlemi yapar.
 *
 * Aslında decode yapmayacağız. Sadece encode yapacağız.
 *
 * Türkçe karakterler ingilizce karşılığına, boşluklar ise alt çicgiye dönüşecek
 *
 * @author Hakan Uygun
 */
public class RafNameEncoder extends BaseRafEncoder {

    protected String encodeSpecialChars(String text) {

        StringBuilder sb = new StringBuilder();
        char[] source = text.toCharArray();
        for (int i = 0; i < source.length; i++) {
            switch (source[i]) {
                case '[':
                    sb.append('(');
                    break;
                case ']':
                    sb.append(')');
                    break;
                case '.':
                case ' ':
                case ':':
                case '\'':
                    sb.append('_');
                    break;
                default:
                    sb.append(source[i]);
            }

        }

        return sb.toString();
    }

}
