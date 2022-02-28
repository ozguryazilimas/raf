package com.ozguryazilim.raf.document.comment;

import com.ozguryazilim.raf.entities.RafDocumentComment;
import com.ozguryazilim.raf.models.RafObject;
import org.apache.deltaspike.jpa.api.transaction.Transactional;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class RafDocumentCommentService {

    //Cache
    private Map<String, List<RafDocumentComment>> comments = new HashMap<>();

    @Inject
    private RafDocumentCommentRepository repository;

    @PostConstruct
    private void init() {
        loadComments();
    }

    public List<RafDocumentComment> getCommentsByNodeId(String nodeId) {
        return repository.findByNodeId(nodeId);
    }

    public List<RafDocumentComment> getCommentsByRafObject(RafObject rafObject) {
        return repository.findByNodeId(rafObject.getId());
    }

    @Transactional
    public void saveComment(RafDocumentComment comment) {
        repository.save(comment);
    }

    @Transactional
    public void deleteComment(RafDocumentComment comment) {
        repository.remove(comment);
    }

    private void loadComments() {
        comments = repository.findAll().stream().collect(Collectors.groupingBy(RafDocumentComment::getNodeId));
    }

    public Map<String, List<RafDocumentComment>> getComments() {
        return comments;
    }

    public void updateDocumentComments(String nodeId) {
        comments.put(nodeId, getCommentsByNodeId(nodeId));
    }
}