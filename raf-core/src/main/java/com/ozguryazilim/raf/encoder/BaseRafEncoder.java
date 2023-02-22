package com.ozguryazilim.raf.encoder;

import org.apache.commons.lang3.StringUtils;

public abstract class BaseRafEncoder implements RafEncoder {

    public String encode(String text) {
        StringBuilder sb = new StringBuilder();
        String stripedText = StringUtils.stripAccents(text);
        char[] source = stripedText.toCharArray();
        for (int i = 0; i < source.length; i++) {
            switch (source[i]) {
                case 'ğ':
                    sb.append('g');
                    break;
                case 'ı':
                    sb.append('i');
                    break;
                case 'ü':
                    sb.append('u');
                    break;
                case 'ş':
                    sb.append('s');
                    break;
                case 'ç':
                    sb.append('c');
                    break;
                case 'ö':
                    sb.append('o');
                    break;
                case 'Ğ':
                    sb.append('G');
                    break;
                case 'İ':
                    sb.append('I');
                    break;
                case 'Ü':
                    sb.append('U');
                    break;
                case 'Ş':
                    sb.append('S');
                    break;
                case 'Ç':
                    sb.append('C');
                    break;
                case 'Ö':
                    sb.append('O');
                    break;
                case ' ':
                    sb.append('_');
                    break;
                case '[':
                    sb.append('(');
                    break;
                case ']':
                    sb.append(')');
                    break;
                case ':':
                    sb.append('_');
                    break;
                case '"':
                    sb.append('_');
                    break;
                default:
                    sb.append(source[i]);
            }
        }

        return encodeSpecialChars(sb.toString());
    }

    public String decode(String encodedText) {
        //Geriye dönüş için özel bir şey yapmıyoruz.
        return encodedText;
    }

    protected abstract String encodeSpecialChars(String text);

}
