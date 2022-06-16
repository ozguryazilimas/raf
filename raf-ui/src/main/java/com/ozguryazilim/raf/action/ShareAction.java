package com.ozguryazilim.raf.action;

import com.ozguryazilim.raf.config.ActionPages;
import com.ozguryazilim.raf.email.EmailNotificationService;
import com.ozguryazilim.raf.entities.RafShare;
import com.ozguryazilim.raf.enums.ShareTime;
import com.ozguryazilim.raf.events.EventLogCommandBuilder;
import com.ozguryazilim.raf.generators.SimplePasswordGenerator;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Action(dialog = ActionPages.ShareDocumentDialog.class,
        icon = "fa-share-alt",
        capabilities = {ActionCapability.Ajax, ActionCapability.DetailViews},
        excludeMimeType = "raf/folder")
public class ShareAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(ShareAction.class);

    @Inject
    private RafShareService shareService;

    @Inject
    private Identity identity;

    @Inject
    private CommandSender commandSender;

    @Inject
    private EmailNotificationService emailNotificationService;

    private SimplePasswordGenerator simplePasswordGenerator;

    private RafShare rafShare;

    private ShareTime shareTime = ShareTime.SHARE_LIMITLESS;

    public RafShare getRafShare() {
        return rafShare;
    }

    public void setRafShare(RafShare rafShare) {
        this.rafShare = rafShare;
    }

    public ShareTime getShareTime() {
        return shareTime;
    }

    public void setShareTime(ShareTime shareTime) {
        this.shareTime = shareTime;
    }

    @Override
    protected void initActionModel() {
        if (simplePasswordGenerator == null) {
            try {
                simplePasswordGenerator = new SimplePasswordGenerator();
            } catch (NoSuchAlgorithmException e) {
                LOG.error("Could not create password generator", e);
            }
        }
        rafShare = new RafShare();
        rafShare.setNodeId(getContext().getSelectedObject().getId());
        rafShare.setSharedBy(identity.getLoginName());
        rafShare.setPassword(simplePasswordGenerator.generatePassword());
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    protected boolean finalizeAction() {
        try {
            Date current = new Date();
            rafShare.setStartDate(current);
            if (shareTime != ShareTime.SHARE_LIMITLESS) {
                rafShare.setEndDate(new Date(current.getTime() + shareTime.millisecond()));
            }
            RafShare result = shareService.share(rafShare);
            String filename = getContext().getSelectedObject().getName();
            String link = UrlUtils.getDocumentShareURL(result.getToken());
            String password = result.getPassword();
            emailNotificationService.sendEmailToSharedContacts(result, filename);
            LOG.info("Document {} has been successfully shared." +
                    "Link: {}, Password: {}", filename, link, password);
            commandSender.sendCommand(EventLogCommandBuilder.forRaf("RAF")
                    .eventType("ShareDocument")
                    .forRafObject(getContext().getSelectedObject())
                    .message("event.ShareDocument$%&" + identity.getUserName() + "$%&" + filename)
                    .user(identity.getLoginName())
                    .build());
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