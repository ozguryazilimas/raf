package com.ozguryazilim.raf.entities;

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
 * Diğer dokuman yönetim sistemi uygulamalarından içeri alınan dokuman tipleri.
 *
 */
@Entity
@Table(name = "SAVED_SEARCH")
public class SavedSearch extends EntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "genericSeq")
    @Column(name = "ID")
    private Long id;

    @Column(name = "MEMBER_NAME", length = 200, nullable = false)
    @NotNull
    @Size(min = 1, max = 200)
    private String memberName;

    @Column(name = "SEARCH_NAME", length = 200, nullable = false)
    @NotNull
    @Size(min = 1, max = 200)
    private String searchName;

    @Column(name = "SEARCH", length = 20000, nullable = false)
    @NotNull
    @Size(min = 1, max = 20000)
    private String search;

    public Long getId() {
        return id;
    }

    public String getMemberName() {
        return memberName;
    }

    public String getSearchName() {
        return searchName;
    }

    public String getSearch() {
        return search;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public void setSearchName(String searchName) {
        this.searchName = searchName;
    }

    public void setSearch(String search) {
        this.search = search;
    }

}
