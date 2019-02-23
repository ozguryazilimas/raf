package com.ozguryazilim.raf.entities;

import com.ozguryazilim.telve.entities.EntityBase;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "RAF_DEPARTMENT_MEMBER")
public class RafDepartmentMember extends EntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "genericSeq")
    @Column(name = "ID")
    private Long id;

    @OneToOne
    @JoinColumn(name = "DEPARTMENT_ID", foreignKey = @ForeignKey(name = "FK_RDM_DEPTID"))
    private RafDepartment department;

    @Column(name = "MEMBER_NAME", length = 100, nullable = false)
    @NotNull
    @Size(min = 1, max = 100)
    private String memberName;

    @Column(name = "MEMBER_TYPE")
    @Enumerated(EnumType.STRING)
    private RafMemberType memberType;

    @Column(name = "ROLE", length = 100)
    @Size(min = 1, max = 100)
    private String role;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RafDepartment getDepartment() {
        return department;
    }

    public void setDepartment(RafDepartment department) {
        this.department = department;
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
