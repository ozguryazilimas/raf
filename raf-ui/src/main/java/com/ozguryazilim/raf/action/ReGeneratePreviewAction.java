package com.ozguryazilim.raf.action;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.events.RafCheckInEvent;
import com.ozguryazilim.raf.events.RafUploadEvent;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.uploader.ui.FileUploadDialog;
import me.desair.tus.server.TusFileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@Action(icon = "fa-refresh",
        capabilities = {ActionCapability.Ajax, ActionCapability.CollectionViews},
        includedMimeType = "raf/folder",
        order = 1)
public class ReGeneratePreviewAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(ReGeneratePreviewAction.class);

    @Inject
    private RafService rafService;

    @Inject
    private Identity identity;

    @Inject
    private RafMemberService memberService;

    @Inject
    private RafPathMemberService rafPathMemberService;

    @Override
    public boolean applicable(boolean forCollection) {
        try {
            boolean permission = false;

            if (getContext().getSelectedObject() != null && !Strings.isNullOrEmpty(identity.getLoginName()) && !Strings.isNullOrEmpty(getContext().getSelectedObject().getPath()) && rafPathMemberService.hasMemberInPath(identity.getLoginName(), getContext().getSelectedObject().getPath())) {
                permission = rafPathMemberService.hasWriteRole(identity.getLoginName(), getContext().getSelectedObject().getPath());
            } else {
                permission = getContext().getSelectedRaf().getId() > 0 && memberService.hasWriteRole(identity.getLoginName(), getContext().getSelectedRaf());
            }

            return permission && super.applicable(forCollection);
        } catch (RafException ex) {
            LOG.error("Error", ex);
            return super.applicable(forCollection);
        }
    }

    @Override
    protected boolean finalizeAction() {
        try {
            rafService.reGenerateObjectPreviews(getContext().getCollection().getItems());
        } catch (RafException e) {
            e.printStackTrace();
            return false;
        }

        return super.finalizeAction();
    }
}
