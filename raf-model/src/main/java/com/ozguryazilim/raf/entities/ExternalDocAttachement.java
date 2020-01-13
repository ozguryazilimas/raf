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
 * Diğer dokuman yönetim sistemi uygulamalarından içeri alınan dokuman ekleri.
 *
 */
@Entity
@Table(name = "EXTERNAL_DOC_ATTACHEMENT")
public class ExternalDocAttachement extends EntityBase implements Cloneable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "genericSeq")
    @Column(name = "ID")
    private Long id;

    @Column(name = "PARENT_RAF_FILE_PATH", length = 1000, nullable = false)
    @NotNull
    @Size(min = 1, max = 1000)
    private String parentRafFilePath;

    @Column(name = "RAF_FILE_PATH", length = 1000, nullable = false)
    @NotNull
    @Size(min = 1, max = 1000)
    private String rafFilePath;

    @Column(name = "PARENT_RAF_FILE_ID", length = 200, nullable = false)
    @NotNull
    @Size(min = 1, max = 200)
    private String parentRafFileId;

    @Column(name = "RAF_FILE_ID", length = 200, nullable = false)
    @NotNull
    @Size(min = 1, max = 200)
    private String rafFileId;

    public Long getId() {
        return id;
    }

    public String getParentRafFileId() {
        return parentRafFileId;
    }

    public String getParentRafFilePath() {
        return parentRafFilePath;
    }

    public void setParentRafFileId(String parentRafFileId) {
        this.parentRafFileId = parentRafFileId;
    }

    public String getRafFileId() {
        return rafFileId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setParentRafFilePath(String parentRafFilePath) {
        this.parentRafFilePath = parentRafFilePath;
    }

    public String getRafFilePath() {
        return rafFilePath;
    }

    public void setRafFileId(String rafFileId) {
        this.rafFileId = rafFileId;
    }

    public void setRafFilePath(String rafFilePath) {
        this.rafFilePath = rafFilePath;
    }

    public Object clone() throws
            CloneNotSupportedException {
        return super.clone();
    }
}
