package com.ozguryazilim.raf.ui.utils;

import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.document.comment.RafDocumentCommentService;
import com.ozguryazilim.raf.entities.RafDocumentComment;
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
        return rafDocumentCommentService.getComments().get(rafContext.getSelectedObject().getId());
    }

    @Transactional
    public void saveComment() {
        this.docComment.setDate(new Date());
        this.docComment.setCommentOwner(identity.getLoginName());
        this.docComment.setNodeId(rafContext.getSelectedObject().getId());

        rafDocumentCommentService.saveComment(this.docComment);
        rafDocumentCommentService.updateDocumentComments(rafContext.getSelectedObject().getId());

        this.docComment = new RafDocumentComment();
    }
    @Transactional
    public void updateComment() {
        rafDocumentCommentService.saveComment(this.editedDocComment);
        rafDocumentCommentService.updateDocumentComments(rafContext.getSelectedObject().getId());

        this.docComment = new RafDocumentComment();
    }

    @Transactional
    public void deleteComment(RafDocumentComment comment) {
        rafDocumentCommentService.deleteComment(comment);

        rafDocumentCommentService.updateDocumentComments(rafContext.getSelectedObject().getId());
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
