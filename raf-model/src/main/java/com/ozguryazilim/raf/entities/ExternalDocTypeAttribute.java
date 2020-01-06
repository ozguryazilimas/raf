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
 * Diğer dokuman yönetim sistemi uygulamalarından içeri alınan dokuman tiplerine ait özel alanlar.
 *
 */
@Entity
@Table(name = "EXTERNAL_DOC_TYPE_ATTRIBUTE")
public class ExternalDocTypeAttribute extends EntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "genericSeq")
    @Column(name = "ID")
    private Long id;

    @OneToOne
    @JoinColumn(name = "DOCUMENT_TYPE_ID", foreignKey = @ForeignKey(name = "FK_EDTA_EDTID"))
    private ExternalDocType documentType;

    @Column(name = "ATTRIBUTE_NAME", length = 200, nullable = false)
    @NotNull
    @Size(min = 1, max = 200)
    private String attributeName;

    public ExternalDocType getDocumentType() {
        return documentType;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public Long getId() {
        return id;
    }

    public void setDocumentType(ExternalDocType documentType) {
        this.documentType = documentType;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

}
