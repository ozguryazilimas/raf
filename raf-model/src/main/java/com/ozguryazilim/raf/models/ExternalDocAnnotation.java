package com.ozguryazilim.raf.models;

import java.io.Serializable;
import java.util.Date;

/**
 * Diğer dokuman yönetim sistemi uygulamalarından içeri alınan dokuman ile
 * ilişkili notlar.
 *
 */
public class ExternalDocAnnotation implements Serializable {

    private Integer orderNo;

    private Date annotationDate;

    private String annotation;

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

    public void setAnnotationDate(Date annotationDate) {
        this.annotationDate = annotationDate;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public void setAnnotationUser(String annotationUser) {
        this.annotationUser = annotationUser;
    }

    public Integer getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Integer orderNo) {
        this.orderNo = orderNo;
    }

}
