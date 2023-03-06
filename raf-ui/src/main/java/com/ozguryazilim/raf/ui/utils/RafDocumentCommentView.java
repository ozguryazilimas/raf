package com.ozguryazilim.raf.ui.utils;

import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.document.comment.RafDocumentCommentService;
import com.ozguryazilim.raf.entities.RafDocumentComment;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.telve.auth.Identity;
import org.apache.deltaspike.jpa.api.transaction.Transactional;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Named
@ViewScoped
public class RafDocumentCommentView implements Serializable {

    private RafDocumentComment docComment = new RafDocumentComment();
    private RafDocumentComment editedDocComment = new RafDocumentComment();

    private SimpleDateFormat hourDateFormat = new SimpleDateFormat("HH:mm");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    @Inject
    private RafDocumentCommentService rafDocumentCommentService;

    @Inject
    private RafContext rafContext;

    @Inject
    private Identity identity;

    public String getCommentHour(RafDocumentComment comment) {
        return hourDateFormat.format(comment.getDate());
    }

    public String getCommentDate(RafDocumentComment comment) {
        return dateFormat.format(comment.getDate());
    }

    public List<RafDocumentComment> getObjectComments() {
        if (rafContext != null && rafContext.getSelectedObject() != null && rafContext.getSelectedObject().getId() != null) {
            return rafDocumentCommentService.getComments().get(rafContext.getSelectedObject().getId());
        } else {
            return null;
        }
    }

    @Transactional
    public void saveComment(RafObject obj) {
        this.docComment.setDate(new Date());
        this.docComment.setCommentOwner(identity.getLoginName());
        this.docComment.setNodeId(obj.getId());

        rafDocumentCommentService.saveComment(this.docComment);
        rafDocumentCommentService.updateDocumentComments(obj.getId());

        this.docComment = new RafDocumentComment();
    }

    @Transactional
    public void updateComment(RafObject obj) {
        rafDocumentCommentService.saveComment(this.editedDocComment);
        rafDocumentCommentService.updateDocumentComments(obj.getId());

        this.docComment = new RafDocumentComment();
    }

    @Transactional
    public void deleteComment(RafDocumentComment comment, RafObject obj) {
        rafDocumentCommentService.deleteComment(comment);

        rafDocumentCommentService.updateDocumentComments(obj.getId());
        this.docComment = new RafDocumentComment();
    }

    public RafDocumentComment getCurrentComment() {
        return this.docComment;
    }

    public RafDocumentComment getEditedDocComment() {
        return editedDocComment;
    }

    public void setEditedDocComment(RafDocumentComment editedDocComment) {
        this.editedDocComment = editedDocComment;
    }

    public boolean canEditComment(RafDocumentComment comment) {
        return identity.getLoginName().equals(comment.getCommentOwner());
    }

}
