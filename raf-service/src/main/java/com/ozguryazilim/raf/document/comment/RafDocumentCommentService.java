package com.ozguryazilim.raf.document.comment;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.entities.RafDocumentComment;
import com.ozguryazilim.raf.events.EventLogCommandBuilder;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.utils.RafPathUtils;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.messagebus.command.CommandSender;
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

    @Inject
    private CommandSender commandSender;

    @Inject
    private RafService rafService;

    @Inject
    private Identity identity;

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

        try {
            RafObject commentedDocument = rafService.getRafObject(comment.getNodeId());
            commandSender.sendCommand(
                EventLogCommandBuilder.forRaf(RafPathUtils.getRafCodeByPath(commentedDocument.getPath()))
                .eventType("DocumentComment.add")
                .forRafObject(commentedDocument)
                .message("event.DocumentComment.add$%&" + identity.getUserName() + "$%&" + commentedDocument.getTitle())
                .user(identity.getLoginName())
                .build()
            );
        } catch (RafException e) {
            throw new RuntimeException(e);
        }
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