package com.ozguryazilim.raf.models;

import java.io.Serializable;

/**
 * Diğer dokuman yönetim sistemi uygulamalarından içeri alınan dokuman tiplerine
 * ait özel alanlar.
 *
 */
public class ExternalDocTypeAttributeValue implements Serializable {

    private String externalDocTypeAttribute;

    private String value;

    public String getExternalDocTypeAttribute() {
        return externalDocTypeAttribute;
    }

    public void setExternalDocTypeAttribute(String externalDocTypeAttribute) {
        this.externalDocTypeAttribute = externalDocTypeAttribute;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
