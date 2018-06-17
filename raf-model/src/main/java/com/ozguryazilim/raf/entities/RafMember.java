/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.entities;

import com.ozguryazilim.telve.entities.EntityBase;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Raf üyelerinin bilgilerini ve üyelik rollerini tutar.
 * 
 * @author Hakan Uygun
 */
@Entity
@Table(name = "RAF_MEMBER")
public class RafMember extends EntityBase{
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "genericSeq")
    @Column(name = "ID")
    private Long id;
    
    @ManyToOne()
    @JoinColumn(name = "RAF_ID", foreignKey = @ForeignKey(name = "FK_RM_RAFID"))
    private RafDefinition raf;
    
    /**
     * Üye kullanıcının loginname'i
     */
    @Column(name = "MEMBER_NAME", length= 100, nullable = false )
    @NotNull @Size(min = 1, max = 100) 
    private String memberName; 
    
    /**
     * Üye tipi
     */
    @Column(name = "MEMBER_TYPE", nullable = false )
    @Enumerated(EnumType.STRING)
    private RafMemberType memberType;
    
    /**
     * Üye kullanıcının bu raf içerisinde rolü
     */
    @Column(name = "ROLE", length= 100, nullable = false )
    @NotNull @Size(min = 1, max = 100) 
    private String role; 

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RafDefinition getRaf() {
        return raf;
    }

    public void setRaf(RafDefinition raf) {
        this.raf = raf;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public RafMemberType getMemberType() {
        return memberType;
    }

    public void setMemberType(RafMemberType memberType) {
        this.memberType = memberType;
    }

    

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    
    
}
