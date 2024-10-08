package com.ozguryazilim.raf.action;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.events.RafFolderChangeEvent;
import com.ozguryazilim.raf.events.RafFolderDataChangeEvent;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.messages.FacesMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author oyas
 */
@Action(icon = "fa-paste",
        capabilities = {ActionCapability.Ajax, ActionCapability.CollectionViews, ActionCapability.NeedClipboard},
        includedMimeType = "raf/folder",
        actionPermission = "insert",
        group = 10,
        order = 3)
public class PasteAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(PasteAction.class);

    @Inject
    private RafService rafService;

    @Inject
    private Event<RafFolderDataChangeEvent> folderDataChangeEvent;

    @Inject
    private Event<RafFolderChangeEvent> folderChangeEvent;

    @Inject
    private Identity identity;

    @Inject
    private RafMemberService memberService;

    @Inject
    private RafPathMemberService rafPathMemberService;

    @Override
    public boolean applicable(boolean forCollection) {
        try {

            if(getContext().getSelectedObject() != null) {
                return cache.get(getContext().getSelectedObject().getId() + identity.getLoginName(), () -> {
                    boolean permission;
                    if (!Strings.isNullOrEmpty(identity.getLoginName()) && !Strings.isNullOrEmpty(getContext().getSelectedObject().getPath()) && rafPathMemberService.hasMemberInPath(identity.getLoginName(), getContext().getSelectedObject().getPath())) {
                        permission = rafPathMemberService.hasWriteRole(identity.getLoginName(), getContext().getSelectedObject().getPath());
                    } else {
                        permission = memberService.hasWriteRole(identity.getLoginName(), getContext().getSelectedRaf());
                    }
                    return permission && super.applicable(forCollection);
                });
            } else if(getContext().getSelectedRaf() != null) {
                return cache.get(getContext().getSelectedRaf().getId() + identity.getLoginName(), () -> {
                    boolean permission = memberService.hasWriteRole(identity.getLoginName(), getContext().getSelectedRaf());
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
    protected void initActionModel() {

        if (getContext().getClipboardAction() == null) {
            return;
        }
        if (getContext().getClipboard().isEmpty()) {
            return;
        }

        LOG.info("Paste {} : {}", getContext().getClipboardAction().getName(), getContext().getClipboard());

        //TODO: aslında asıl komuta geri dönüp onu çalıştırmak daha mantıklı olacak. Böylece özelleşmiş paste komutları yazılabilir. ( link v.s. için mesela )
        if (getContext().getClipboardAction() instanceof CopyAction) {
            try {
                rafService.copyObject(getContext().getClipboard(), getContext().getSelectedObject());
                //FIXME: Burada RafEventLog çalıştırılmalı
            } catch (RafException ex) {
                //FIXME: i18n
                LOG.error("Cannot copy", ex);
                FacesMessages.error("Cannot copy", ex.getLocalizedMessage());
            }
        } else if (getContext().getClipboardAction() instanceof CutAction) {
            try {
                rafService.moveObject(getContext().getClipboard(), getContext().getSelectedObject());
                //FIXME: Burada RafEventLog çalıştırılmalı
            } catch (RafException ex) {
                //FIXME: i18n
                LOG.error("Cannot move", ex);
                FacesMessages.error("Cannot move", ex.getLocalizedMessage());
            }
        }

        folderChangeEvent.fire(new RafFolderChangeEvent(getContext().getClipboardAction()));

        if (getContext().getClipboard().stream().anyMatch(o -> o instanceof RafFolder)) {
            folderDataChangeEvent.fire(new RafFolderDataChangeEvent());
        }
    }

    @Override
    protected boolean finalizeAction() {

        getContext().setClipboardAction(null);
        getContext().getClipboard().clear();

        return super.finalizeAction();
    }
}
