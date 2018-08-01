/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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

/**
 *
 * @author oyas
 */
@Entity
@Table(name = "RAF_EVENTS")
public class RafEventLog extends EntityBase{
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "genericSeq")
    @Column(name = "ID")
    private Long id;

    /**
     * Log'a neden olan kullanıcı
     */
    @Column(name = "USERNAME")
    private String username;
    
    /**
     * Log üretilme zamanı
     */
    @Column(name = "LOG_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date logTime;
    
    /**
     * Log mesajı.
     * Bu mesaj Telve FormatedMessage yapısında olacaktır.
     */
    @Column(name = "MESSAGE")
    private String message;
    
    /**
     * Ek açıklama.
     * Bu mesaj Telve FormatedMessage yapısında olacaktır.
     */
    @Column(name = "INFO")
    private String info;
    
    /**
     * Log için ana grup kodu.
     * 
     * Bu bilgi sorgularda hız için kullanılacak. 
     * Normal şartlarda RafCode olacak içerisinde ama PROCESS, TASK gibi fix değerler de olabilir.
     * 
     */
    @Column(name = "CODE")
    private String code;
    
    /**
     * Log'un üretildiği yere referans.
     * 
     * JCR açısından NODE_ID olacak
     * BPM için ise task_id ya da processInstanceId olacak
     */
    @Column(name = "REFID")
    private String refId;
    
    /**
     * Log'un üretildiği nesne için path.
     * 
     * jcr:/RAF/deneme/test.pdf
     * bpm:/icyazisma/bilginotu
     * 
     */
    @Column(name = "PATH")
    private String path;
    
    /**
     * Event tipleri.
     * 
     * UI üzerinde filtre ve sunum için kullanılabilir.
     * 
     * Örnek:
     * CreateFolder, UploadFile, VersionUpdate, FileDonwload
     * RecordProcessStarted, RecordProcessEnded, TaskAssigned
     */
    @Column(name = "TYPE")
    private String type;
    
    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getLogTime() {
        return logTime;
    }

    public void setLogTime(Date logTime) {
        this.logTime = logTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    
    
}
