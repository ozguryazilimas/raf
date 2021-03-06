package com.ozguryazilim.raf.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Klasör/Belge yapıları için temel model.
 * 
 * Bu modeller JCR ile uygulama arayüzleri arasında DTO olarak görev yaparlar.
 * 
 * RafObject en temel model sınıf. Dorudan kendisinin bir anlamı yok mutlaka alt sınıfları olması gerek.
 * 
 * Bilinen alt sınıflar RafNode, RafFolder, RafDocument
 * 
 * @author Hakan Uygun
 */
public abstract class RafObject implements Serializable{
    
    private String id;
    private String name;
    private String title;
    private String mimeType;
    private String path;
    private String info;
    private String parentId;
    private Long length = 0l;
    
    private String category;
    private String categoryPath;
    private Long categoryId;
    private List<String> tags = new ArrayList<>();
    private String color;
    private Integer rating;
    
    private String createBy;
    private Date createDate;
    private String updateBy;
    private Date updateDate;

    private String version = "1.0";
    private Boolean checkedout = Boolean.FALSE;
    private Boolean versionable = Boolean.FALSE;
    
    private List<RafMetadata> metadatas = new ArrayList<>();
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public List<RafMetadata> getMetadatas() {
        return metadatas;
    }

    public void setMetadatas(List<RafMetadata> metadatas) {
        this.metadatas = metadatas;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategoryPath() {
        return categoryPath;
    }

    public void setCategoryPath(String categoryPath) {
        this.categoryPath = categoryPath;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    
    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Boolean getCheckedout() {
        return checkedout;
    }

    public void setCheckedout(Boolean checkedout) {
        this.checkedout = checkedout;
    }

    public Boolean getVersionable() {
        return versionable;
    }

    public void setVersionable(Boolean versionable) {
        this.versionable = versionable;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RafObject other = (RafObject) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getTitle();
    }
    
    
}
