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
@Table(name = "EXTERNAL_DOC_TYPE_ATTRIBUTE_LIST")
public class ExternalDocTypeAttributeList extends EntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "genericSeq")
    @Column(name = "ID")
    private Long id;

    @Column(name = "ATTRIBUTE_NAME", length = 200, nullable = false)
    @NotNull
    @Size(min = 1, max = 200)
    private String attributeName;

    @Column(name = "LIST_VALUE", length = 200, nullable = false)
    @NotNull
    @Size(min = 1, max = 200)
    private String listValue;

    public String getAttributeName() {
        return attributeName;
    }

    public Long getId() {
        return id;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getListValue() {
        return listValue;
    }

    public void setListValue(String listValue) {
        this.listValue = listValue;
    }

}
