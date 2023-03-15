package com.ozguryazilim.raf.entities;

import com.ozguryazilim.telve.entities.TreeNodeEntityBase;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "RAF_DEPARTMENT")
public class RafDepartment extends TreeNodeEntityBase<RafDepartment> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "genericSeq")
    @Column(name = "ID")
    private Long id;

    @OneToMany(mappedBy = "department", cascade = {CascadeType.ALL}, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RafDepartmentMember> members = new ArrayList<>();

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
