package com.ozguryazilim.raf.entities;

import com.ozguryazilim.telve.entities.EntityBase;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Diğer dokuman yönetim sistemi uygulamalarından içeri alınan dokuman ile
 * ilişkili notlar.
 *
 */
@Entity
@Table(name = "EXTERNAL_DOC_ANNOTATION")
public class ExternalDocAnnotation extends EntityBase implements Cloneable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "genericSeq")
    @Column(name = "ID")
    private Long id;

    @Column(name = "RAF_FILE_PATH", length = 1000, nullable = false)
    @NotNull
    @Size(min = 1, max = 1000)
    private String rafFilePath;

    @Column(name = "RAF_FILE_ID", length = 200, nullable = false)
    @NotNull
    @Size(min = 1, max = 200)
    private String rafFileId;

    @Column(name = "ORDER_NO", nullable = false)
    @NotNull
    private Integer orderNo;

    @Column(name = "ANNOTATION_DATE", nullable = false)
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date annotationDate;

    @Column(name = "ANNOTATION", length = 1000, nullable = false)
    @NotNull
    @Size(min = 1, max = 1000)
    private String annotation;

    @Column(name = "ANNOTATION_USER", length = 200, nullable = false)
    @NotNull
    @Size(min = 1, max = 200)
    private String annotationUser;

    public Date getAnnotationDate() {
        return annotationDate;
    }

    public String getAnnotation() {
        return annotation;
    }

    public String getAnnotationUser() {
        return annotationUser;
    }

    public Long getId() {
        return id;
    }

    public String getRafFileId() {
        return rafFileId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAnnotationDate(Date annotationDate) {
        this.annotationDate = annotationDate;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public void setAnnotationUser(String annotationUser) {
        this.annotationUser = annotationUser;
    }

    public String getRafFilePath() {
        return rafFilePath;
    }

    public void setRafFileId(String rafFileId) {
        this.rafFileId = rafFileId;
    }

    public void setRafFilePath(String rafFilePath) {
        this.rafFilePath = rafFilePath;
    }

    public Integer getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Integer orderNo) {
        this.orderNo = orderNo;
    }

    public Object clone() throws
            CloneNotSupportedException {
        return super.clone();
    }
}
