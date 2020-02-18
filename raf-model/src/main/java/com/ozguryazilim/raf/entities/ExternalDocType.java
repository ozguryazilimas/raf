package com.ozguryazilim.raf.entities;

import com.ozguryazilim.telve.entities.EntityBase;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Diğer dokuman yönetim sistemi uygulamalarından içeri alınan dokuman tipleri.
 *
 */
@Entity
@Table(name = "EXTERNAL_DOC_TYPE")
public class ExternalDocType extends EntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "genericSeq")
    @Column(name = "ID")
    private Long id;

    @Column(name = "DOCUMENT_TYPE", length = 200, nullable = false)
    @NotNull
    @Size(min = 1, max = 200)
    private String documentType;

    public Long getId() {
        return id;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

}
