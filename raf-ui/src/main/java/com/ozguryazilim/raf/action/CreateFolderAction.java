package com.ozguryazilim.raf.action;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.config.ActionPages;
import com.ozguryazilim.raf.encoder.RafEncoder;
import com.ozguryazilim.raf.encoder.RafEncoderFactory;
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
import javax.enterprise.event.Event;
import javax.inject.Inject;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Seçili klasör içine yeni bir kalsör ekler.
 *
 * @author Hakan Uygun
 */
@Action(dialog = ActionPages.CreateFolderDialog.class,
        icon = "fa-plus",
        capabilities = {ActionCapability.Ajax, ActionCapability.CollectionViews},
        includedMimeType = "raf/folder",
        actionPermission = "insert",
        order = 1)
public class CreateFolderAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(CreateFolderAction.class);

    @Inject
    private RafService rafService;

    @Inject
    private Event<RafFolderDataChangeEvent> folderCreateEvent;

    @Inject
    private Event<RafFolderChangeEvent> folderChangeEvent;

    @Inject
    private Identity identity;

    @Inject
    private RafMemberService memberService;

    @Inject
    private RafPathMemberService rafPathMemberService;

    private RafFolder folder;

    @Override
    public boolean applicable(boolean forCollection) {
        try {
            boolean permission = false;
            String createFolderPermission = ConfigResolver.getPropertyValue("createFolder.permission", "hasWrite");

            if (getContext().getSelectedObject() != null && !Strings.isNullOrEmpty(identity.getLoginName()) && !Strings.isNullOrEmpty(getContext().getSelectedObject().getPath()) && rafPathMemberService.hasMemberInPath(identity.getLoginName(), getContext().getSelectedObject().getPath())) {
                permission = "hasWrite".equals(createFolderPermission) ? rafPathMemberService.hasWriteRole(identity.getLoginName(), getContext().getSelectedObject().getPath()) : rafPathMemberService.hasDeleteRole(identity.getLoginName(), getContext().getSelectedObject().getPath());
            } else {
                permission = getContext().getSelectedRaf().getId() > 0 && "hasWrite".equals(createFolderPermission) ? memberService.hasWriteRole(identity.getLoginName(), getContext().getSelectedRaf()) : memberService.hasDeleteRole(identity.getLoginName(), getContext().getSelectedRaf());
            }

            return permission && super.applicable(forCollection);
        } catch (RafException ex) {
            LOG.error("Error", ex);
            return super.applicable(forCollection);
        }
    }

    @Override
    protected void initActionModel() {
        folder = new RafFolder();

        folder.setParentId(getContext().getCollection().getId());
    }

    @Override
    protected boolean finalizeAction() {
        folder.setPath(getContext().getCollection().getPath() + "/" + folder.getName());
        if (!rafService.checkRafName(folder.getTitle())) {
            return false;
        }
        try {
            rafService.createFolder(folder);
        } catch (RafException ex) {
            //TODO: i18n
            FacesMessages.error("Raf Tanımlaması Yapılamadı", ex.getMessage());
            LOG.error("Raf Tanımlaması Yapılamadı", ex);
            return false;
        }

        folderCreateEvent.fire(new RafFolderDataChangeEvent());
        folderChangeEvent.fire(new RafFolderChangeEvent());

        return true;
    }

    public RafFolder getFolder() {
        return folder;
    }

    public void setFolder(RafFolder folder) {
        this.folder = folder;
    }

    public void onNameChange() {
        if (rafService.checkRafName(folder.getTitle())) {
            RafEncoder encoder = RafEncoderFactory.getDirNameEncoder();
            //TODO aslında code içinde bir şey var ise bunu yapmasak mı?
            folder.setName(encoder.encode(folder.getTitle()));
        }
    }
}
