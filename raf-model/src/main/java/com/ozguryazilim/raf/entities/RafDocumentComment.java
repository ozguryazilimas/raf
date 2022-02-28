package com.ozguryazilim.raf.entities;

import com.ozguryazilim.telve.entities.EntityBase;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "RAF_DOCUMENT_COMMENT")
public class RafDocumentComment extends EntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "genericSeq")
    @Column(name = "ID")
    private Long id;

    @Column(name = "COMMENT_OWNER", nullable = false)
    @NotNull
    private String commentOwner;

    @Column(name = "COMMENT", nullable = false)
    @NotNull
    private String comment;

    @Column(name = "DATE", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @Column(name = "NODE_ID", nullable = false)
    @NotNull
    private String nodeId;

    public RafDocumentComment() {
    }

    public RafDocumentComment(String commentOwner, String comment, Date date, String nodeId) {
        this.commentOwner = commentOwner;
        this.comment = comment;
        this.date = date;
        this.nodeId = nodeId;
    }

    @Override
    public Long getId() {
        return null;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCommentOwner() {
        return commentOwner;
    }

    public void setCommentOwner(String commentOwner) {
        this.commentOwner = commentOwner;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

}
