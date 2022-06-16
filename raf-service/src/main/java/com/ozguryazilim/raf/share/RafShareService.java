package com.ozguryazilim.raf.share;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.entities.RafShare;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.telve.audit.AuditLogCommand;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.messagebus.command.CommandSender;
import freemarker.template.SimpleDate;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.deltaspike.jpa.api.transaction.Transactional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class RafShareService {

    @Inject
    private RafShareRepository repository;

    @Inject
    private RafService rafService;

    @Inject
    private Identity identity;

    @Inject
    private CommandSender commandSender;

    public RafObject getDocument(String token, String password) throws RafException {

        if (StringUtils.isBlank(token) || StringUtils.isBlank(password)) {
            throw new RafException("code.or.password.empty");
        }

        List<RafShare> results = repository.findByTokenAndPassword(token, password);

        if (CollectionUtils.isNotEmpty(results)) {
            RafShare rafShare = results.get(0);
            if (rafShare.getNodeId() != null && (rafShare.getEndDate() == null || rafShare.getEndDate().after(new Date()))) {
                incrementVisit(rafShare);
                RafObject rafObject = rafService.getRafObject(rafShare.getNodeId());
                AuditLogCommand command = new AuditLogCommand("RAF",
                        Long.MIN_VALUE,
                        rafObject.getId(),
                        "ANONYMOUS_DOWNLOAD",
                        "RAF",
                        identity.getLoginName(),
                String.format("File: %s, Access Token: %s, Password: %s", rafObject.getName(), token, password));
                commandSender.sendCommand(command);
                return rafObject;
            }
            throw new RafException("document.share.time.expired");
        }

        throw new RafException("document.not.found.with.given.password");
    }

    public List<RafShare> get(String nodeId) {
        return repository.findByNodeId(nodeId);
    }

    public List<RafShare> getShareGroup(String shareGroupId) {
        return repository.findByShareGroup(shareGroupId);
    }

    public List<RafShare> getAll() {
        return repository.findAll();
    }

    public List<RafShare> findByLoginName(String loginName) {
        return repository.findActiveSharesBySharedBy(loginName);
    }

    @Transactional
    public void clear(String token) {
        repository.deleteByToken(token);
    }

    @Transactional
    public void clearShareGroup(String groupToken) {
        repository.deleteByShareGroup(groupToken);
    }

    @Transactional
    public void clearOutdated() {
        List<RafShare> all = getAll();
        if (CollectionUtils.isNotEmpty(all)) {
            all.stream()
                    .filter(rs -> (rs.getEndDate() != null && rs.getEndDate().before(new Date())))
                    .forEach(rs -> {
                        clear(rs.getToken());
                        rafShareEndAuditLog(rs);
                    });
        }
    }

    public RafShare share(RafShare share) {
        return repository.saveAndFlush(share);
    }

    public List<RafShare> share(List<RafShare> shares) {
        return shares.stream()
                .map(this::share)
                .collect(Collectors.toList());
    }

    private void incrementVisit(RafShare rafShare) {
        rafShare.setVisit(rafShare.getVisit() + 1);
        repository.saveAndFlush(rafShare);
    }

    public void rafShareEndAuditLog(RafShare rafShare) {
        String user;
        if (identity != null && !Strings.isNullOrEmpty(identity.getLoginName())) {
            user = identity.getLoginName();
        } else {
            user = "SYSTEM";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        StringBuilder sb = new StringBuilder();
        sb.append("Share_start: ")
                .append(rafShare.getStartDate())
                .append(" Share_end: ")
                .append(rafShare.getEndDate() != null ? rafShare.getEndDate() : "-")
                .append(" Share_clear: ")
                .append(sdf.format(new Date()))
                .append(" Visit: ")
                .append(rafShare.getVisit());


        AuditLogCommand command = new AuditLogCommand("RAF",
                Long.MIN_VALUE,
                rafShare.getNodeId(),
                "RAFSHARE_CLEAR",
                "RAF",
                user,
                sb.toString());

        commandSender.sendCommand(command);
    }

}