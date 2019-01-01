package com.ozguryazilim.raf.models;

/**
 * RafDefinition'a denk düşen JCR objesi.
 * 
 * miemType'ı her hazaman için "raf/rafNode" şeklinde döner. jcr tarafında ise nt:unstructered olarak tanımlı.
 * 
 * name alanı RafDefinition üzerinde code'a denktir.
 * title alanı RafDefinition üzerinde name'e denktir.
 * 
 * @author Hakan Uygun
 */
public class RafNode extends RafObject{

    @Override
    public String getMimeType() {
        return RafMimeTypes.RAF_NODE;
    }
    
}
