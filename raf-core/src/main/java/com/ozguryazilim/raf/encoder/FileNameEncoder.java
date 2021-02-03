package com.ozguryazilim.raf.encoder;

import java.util.regex.Pattern;


/**
 * Raf dosya ismi için encode decode işlemi yapar.
 *
 * Aslında decode yapmayacağız. Sadece encode yapacağız.
 *
 * Türkçe karakterler ingilizce karşılığına, boşluklar ise alt çicgiye dönüşecek
 *
 * @author Hakan Uygun
 */
public class FileNameEncoder extends BaseRafEncoder {

    protected String encodeSpecialChars(String text) {

        StringBuilder sb = new StringBuilder();
        //toLowerCase(new Locale("tr")) hepsini küçük harfer çevirmek için eklenebilir

        String[] textArray = text.split("/");
        String fileName = textArray[textArray.length - 1];
        if (fileName.split("\\.").length > 2) {
            String extension = fileName.substring(fileName.lastIndexOf("."));
            fileName = fileName.replaceAll(Pattern.quote(extension), "").replaceAll("\\.", "_").concat(extension);
            textArray[textArray.length - 1] = fileName;
        }
        text = String.join("/", textArray);

        char[] source = text.toCharArray();
        for (int i = 0; i < source.length; i++) {
            switch (source[i]) {
                case '[':
                    sb.append('(');
                    break;
                case ']':
                    sb.append(')');
                    break;
                case ' ':
                case ':':
                case '"':
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
