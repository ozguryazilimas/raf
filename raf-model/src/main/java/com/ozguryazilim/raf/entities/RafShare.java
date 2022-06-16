package com.ozguryazilim.raf.entities;

import com.ozguryazilim.raf.converters.StringListConverter;
import com.ozguryazilim.raf.generators.SimplePasswordGenerator;
import com.ozguryazilim.raf.generators.UUIDGenerator;
import com.ozguryazilim.telve.entities.EntityBase;
import com.sun.istack.Nullable;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.GeneratorType;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "RAF_SHARE")
public class RafShare extends EntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "genericSeq")
    @Column(name = "ID")
    private Long id;

    /**
     * Dökümanın jcr üzerinden ulaşılabilir gerçek node id'si
     */
    @Column(name = "NODE_ID", nullable = false)
    @NotNull
    private String nodeId;


    /**
     * Paylaşan kişinin adı ve soyadı
     */
    @Column(name = "SHARED_BY", length = 100, nullable = false)
    @NotNull
    private String sharedBy;

    /**
     * URL'e koymak üzere otomatik üretilen UUID
     */
    @GeneratorType(type = UUIDGenerator.class, when = GenerationTime.INSERT)
    @Column(name = "TOKEN")
    private String token;

    /**
     * Erişim için otomatik üretilen 6 haneli basit parola
     */
    @Column(name = "password", length = 6)
    private String password;

    /**
     * Dökümanın paylaşıma başlandığı tarih ve saat bilgisi
     */
    @Column(name = "START_DATE", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;

    /**
     * Dökümanın paylaşımının son bulacağı tarih ve saat bilgisi.
     * Not: Süresiz paylaşıma açılmış olabilir
     */
    @Column(name = "END_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    @Nullable
    private Date endDate;

    /**
     * Dökümanın kimlerle paylaşıldığının listesi
     */
    @Column(name = "EMAILS", length = 1000)
    @Convert(converter = StringListConverter.class)
    @Nullable
    private List<String> emails;


    /**
     * Döküman kaç defa indirilmiş?
     */
    @Column(name = "VISIT", length = 1000)
    private int visit = 0;

    /**
     * Ek Bilgi
     */
    @Column(name = "INFO")
    private String info;

    @Column(name = "SHARE_GROUP")
    private String shareGroup;

    @Override
    public Long getId() {
        return null;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getSharedBy() {
        return sharedBy;
    }

    public void setSharedBy(String sharedBy) {
        this.sharedBy = sharedBy;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    public int getVisit() {
        return visit;
    }

    public void setVisit(int visit) {
        this.visit = visit;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getShareGroup() {
        return shareGroup;
    }

    public void setShareGroup(String shareGroup) {
        this.shareGroup = shareGroup;
    }
}