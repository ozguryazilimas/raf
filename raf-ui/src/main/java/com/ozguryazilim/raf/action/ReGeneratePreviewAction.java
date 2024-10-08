package com.ozguryazilim.raf.action;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.events.RafFolderChangeEvent;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafNode;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;
import com.ozguryazilim.telve.auth.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.concurrent.ExecutionException;

@Action(icon = "fa-refresh",
        permissions = {"MANAGER"},
        capabilities = {ActionCapability.Ajax, ActionCapability.CollectionViews, ActionCapability.DetailViews, ActionCapability.Confirmation},
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

    @Inject
    private Event<RafFolderChangeEvent> rafFolderChangeEvent;

    @Override
    public boolean applicable(boolean forCollection) {
        try {
            if (getContext().getSelectedObject() != null) {
                return cache.get(getContext().getSelectedObject().getId() + identity.getLoginName(), () -> {
                    boolean permission;
                    if (!Strings.isNullOrEmpty(identity.getLoginName()) && !Strings.isNullOrEmpty(getContext().getSelectedObject().getPath()) && rafPathMemberService.hasMemberInPath(identity.getLoginName(), getContext().getSelectedObject().getPath())) {
                        permission = rafPathMemberService.hasWriteRole(identity.getLoginName(), getContext().getSelectedObject().getPath());
                    } else {
                        permission = getContext().getSelectedRaf() != null && memberService.hasWriteRole(identity.getLoginName(), getContext().getSelectedRaf());
                    }
                    return permission && super.applicable(forCollection);
                });
            }
            return false;
        } catch (ExecutionException ex) {
            LOG.error("Error", ex);
            return super.applicable(forCollection);
        }
    }

    @Override
    protected boolean finalizeAction() {
        try {
            if (getContext().getSelectedObject() instanceof RafFolder || getContext().getSelectedObject() instanceof RafNode) {
                //aslında folderlarda çalıştırmasak iyi olur.
                rafService.regenerateObjectPreviews(getContext().getSelectedObject().getId());
            } else {
                rafService.regeneratePreview(getContext().getSelectedObject().getId());
            }
            rafFolderChangeEvent.fire(new RafFolderChangeEvent());
        } catch (RafException e) {
            LOG.error("Preview regenaration faild");
            return false;
        }

        return super.finalizeAction();
    }
}
