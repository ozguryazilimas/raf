/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.entities;

import com.ozguryazilim.telve.annotations.BizKey;
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
 * Raf Tanımlama modeli.
 * 
 * Veri tabanın da tutulacak olan parça. Üzerine bir de JCR denkliği olacak.
 * 
 * @author Hakan Uygun
 */
@Entity
@Table(name = "RAF_DEFINITION")
public class RafDefinition extends EntityBase{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "genericSeq")
    @Column(name = "ID")
    private Long id;
    
    /**
     * Raf idenditifer, URL ve benzeri yerlerde bu kullanılacak. 
     */
    @Column(name = "CODE", length = 30, nullable = false, unique = true )
    @NotNull @Size( min = 0, max = 30) 
    private String code;
    
    /**
     * Arayüzlerde kullanılacak olan Raf adı
     */
    @Column(name = "NAME", length= 100, nullable = false )
    @NotNull @Size(min = 1, max = 100) @BizKey
    private String name; 

    
    /**
     * Raf ile ilgili ek açıklama
     */
    @Column(name = "INFO", length= 255)
    private String info; 
    
    /**
     * JCR denkliğinin id'si bu sayede karşılıklı etkileşim kolaylaşır.
     */
    @Column(name = "NODE_ID", length= 255)
    private String nodeId; 

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
        
    

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
    

    
}
