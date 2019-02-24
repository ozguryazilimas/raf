package com.ozguryazilim.raf.entities;

import com.ozguryazilim.telve.entities.TreeNodeEntityBase;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "RAF_DEPARTMENT")
public class RafDepartment extends TreeNodeEntityBase<RafDepartment> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "genericSeq")
    @Column(name = "ID")
    private Long id;

    @OneToMany(mappedBy = "department", cascade = {CascadeType.ALL}, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RafDepartmentMember> members;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<RafDepartmentMember> getMembers() {
        return members;
    }

    public void setMembers(List<RafDepartmentMember> members) {
        this.members = members;
    }
}
