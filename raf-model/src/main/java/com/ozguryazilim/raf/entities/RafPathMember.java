package com.ozguryazilim.raf.entities;

import com.ozguryazilim.telve.entities.EntityBase;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Raf nesnesinin üyelerinin bilgilerini ve üyelik rollerini tutar.
 *
 */
@Entity
@Table(name = "RAF_PATH_MEMBER")
public class RafPathMember extends EntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "genericSeq")
    @Column(name = "ID")
    private Long id;

    @Column(name = "PATH", length = 1000, nullable = false)
    @NotNull
    @Size(min = 1, max = 1000)
    private String path;

    /**
     * Üye kullanıcının loginname'i
     */
    @Column(name = "MEMBER_NAME", length = 100, nullable = false)
    @NotNull
    @Size(min = 1, max = 100)
    private String memberName;

    /**
     * Üye tipi
     */
    @Column(name = "MEMBER_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private RafMemberType memberType;

    /**
     * Üye kullanıcının bu raf içerisinde rolü
     */
    @Column(name = "ROLE", length = 100, nullable = false)
    @NotNull
    @Size(min = 1, max = 100)
    private String role;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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
