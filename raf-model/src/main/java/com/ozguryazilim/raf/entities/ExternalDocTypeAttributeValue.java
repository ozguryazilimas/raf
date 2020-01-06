package com.ozguryazilim.raf.entities;

import com.ozguryazilim.telve.entities.EntityBase;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Diğer dokuman yönetim sistemi uygulamalarından içeri alınan dokuman tiplerine
 * ait özel alanlar.
 *
 */
@Entity
@Table(name = "EXTERNAL_DOC_TYPE_ATTRIBUTE_VALUE")
public class ExternalDocTypeAttributeValue extends EntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "genericSeq")
    @Column(name = "ID")
    private Long id;

    @OneToOne
    @JoinColumn(name = "DOCUMENT_TYPE_ID", foreignKey = @ForeignKey(name = "FK_EDTA_EDTID"))
    private ExternalDocType documentType;

    @OneToOne
    @JoinColumn(name = "ATTRIBUTE_ID", foreignKey = @ForeignKey(name = "FK_EDTAV_EDTAID"))
    private ExternalDocTypeAttribute externalDocTypeAttribute;

    @Column(name = "RAF_FILE_PATH", length = 1000, nullable = false)
    @NotNull
    @Size(min = 1, max = 1000)
    private String rafFilePath;

    @Column(name = "VALUE", length = 500, nullable = false)
    @NotNull
    @Size(min = 1, max = 500)
    private String value;

    public ExternalDocType getDocumentType() {
        return documentType;
    }

    public ExternalDocTypeAttribute getExternalDocTypeAttribute() {
        return externalDocTypeAttribute;
    }

    public Long getId() {
        return id;
    }

    public String getRafFilePath() {
        return rafFilePath;
    }

    public String getValue() {
        return value;
    }

    public void setDocumentType(ExternalDocType documentType) {
        this.documentType = documentType;
    }

    public void setExternalDocTypeAttribute(ExternalDocTypeAttribute externalDocTypeAttribute) {
        this.externalDocTypeAttribute = externalDocTypeAttribute;
    }

    public void setRafFilePath(String rafFilePath) {
        this.rafFilePath = rafFilePath;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
