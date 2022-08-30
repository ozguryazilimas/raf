package com.ozguryazilim.raf.action;

import com.ozguryazilim.raf.config.ActionPages;
import com.ozguryazilim.raf.email.EmailNotificationService;
import com.ozguryazilim.raf.entities.RafShare;
import com.ozguryazilim.raf.enums.ShareTime;
import com.ozguryazilim.raf.events.EventLogCommandBuilder;
import com.ozguryazilim.raf.generators.SimplePasswordGenerator;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.share.RafShareService;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;
import com.ozguryazilim.raf.utils.UrlUtils;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.messagebus.command.CommandSender;
import com.ozguryazilim.telve.messages.FacesMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Action(dialog = ActionPages.ShareMultipleDocumentDialog.class,
        icon = "fa-share-alt",
        excludedSelectionMimeType = "raf/folder",
        capabilities = {ActionCapability.CollectionViews, ActionCapability.MultiSelection, ActionCapability.NeedSelection})
public class MultipleShareAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(MultipleShareAction.class);

    @Inject
    private RafShareService shareService;

    @Inject
    private Identity identity;

    @Inject
    private CommandSender commandSender;

    @Inject
    private EmailNotificationService emailNotificationService;

    private SimplePasswordGenerator simplePasswordGenerator;

    private List<RafShare> rafShareList;

    private List<String> emails;

    private ShareTime shareTime = ShareTime.SHARE_LIMITLESS;

    private String sharePassword;

    public ShareTime getShareTime() {
        return shareTime;
    }

    public void setShareTime(ShareTime shareTime) {
        this.shareTime = shareTime;
    }

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    @Override
    protected void initActionModel() {
        rafShareList = new ArrayList<>();
        if (simplePasswordGenerator == null) {
            try {
                simplePasswordGenerator = new SimplePasswordGenerator();
            } catch (NoSuchAlgorithmException e) {
                LOG.error("Could not create password generator", e);
            }
        }
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    protected boolean finalizeAction() {
        try {
            String rafShareGroup = UUID.randomUUID().toString();
            sharePassword = simplePasswordGenerator.generatePassword();

            getContext().getSeletedItems().stream()
                    .map(RafObject::getId)
                    .forEach(id -> {
                        RafShare rafShare = new RafShare();
                        rafShare.setNodeId(id);
                        rafShare.setSharedBy(identity.getLoginName());
                        rafShare.setPassword(sharePassword);
                        rafShare.setEmails(emails);
                        rafShareList.add(rafShare);
                        if (getContext().getSeletedItems().size() > 1) {
                            rafShare.setShareGroup(rafShareGroup);
                        }
                    });

            Date current = new Date();
            rafShareList.forEach(item -> item.setStartDate(current));
            if (shareTime != ShareTime.SHARE_LIMITLESS) {
                rafShareList.forEach(item -> item.setEndDate(new Date(current.getTime() + shareTime.millisecond())));
            }

            List<RafShare> results = shareService.share(rafShareList);
            List<String> filenames = getContext().getSeletedItems().stream().map(RafObject::getName).collect(Collectors.toList());
            List<String> links = results.stream().map(item -> UrlUtils.getDocumentShareURL(item.getToken())).collect(Collectors.toList());
            String password = sharePassword;

            if (results.size() > 1) {
                emailNotificationService.sendEmailToSharedContacts(results, filenames);
            } else {
                emailNotificationService.sendEmailToSharedContacts(results.get(0), filenames.get(0));
            }

            IntStream.range(0, filenames.size())
                .collect(HashMap::new, (m, i) -> m.put(filenames.get(i), links.get(i)), Map::putAll)
                .forEach((filename, url) -> {
                    LOG.info("Document {} has been successfully shared. Link: {}, Password: {}", filename, url, password);

                    commandSender.sendCommand(EventLogCommandBuilder.forRaf("RAF")
                        .eventType("ShareDocument")
                        .forRafObject(getContext().getSelectedObject())
                        .message("event.ShareDocument$%&" + identity.getUserName() + "$%&" + filename)
                        .user(identity.getLoginName())
                        .build());
                    }
                );


            rafShareList.clear();
            emails.clear();

            return true;
        } catch (Exception ex) {
            FacesMessages.error("raf.share.error", ex.getMessage());
            return false;
        }
    }

    public List<String> suggestIsEmail(String email) {
        String regex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(email);
        return m.find() ? Arrays.asList(email) : null;
    }

}
