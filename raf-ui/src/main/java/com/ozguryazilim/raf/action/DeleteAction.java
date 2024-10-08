package com.ozguryazilim.raf.action;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.events.RafFolderDataChangeEvent;
import com.ozguryazilim.raf.events.RafObjectDeleteEvent;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;
import com.ozguryazilim.raf.utils.RafObjectUtils;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.messages.FacesMessages;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author oyas
 */
@Action(icon = "fa-trash", capabilities = {ActionCapability.Ajax, ActionCapability.CollectionViews, ActionCapability.DetailViews, ActionCapability.MultiSelection, ActionCapability.NeedSelection, ActionCapability.Confirmation},
        group = 11,
        actionPermission = "delete",
        order = 0)
public class DeleteAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteAction.class);

    @Inject
    private RafService rafService;

    @Inject
    private Event<RafObjectDeleteEvent> deleteEvent;

    @Inject
    private Event<RafFolderDataChangeEvent> folderChangeEvent;

    @Inject
    private Identity identity;

    @Inject
    private RafMemberService memberService;

    @Inject
    private RafPathMemberService rafPathMemberService;

    @Override
    public boolean applicable(boolean forCollection) {
        try {
            if (getContext().getSelectedObject() != null) {
                return cache.get(getContext().getSelectedObject().getId() + identity.getLoginName(), () -> {
                    LOG.debug("User: {}, Path: {} ID: {} . Checking delete action applicable for selected object.",
                            identity.getLoginName(),
                            getContext().getSelectedObject().getPath(),
                            getContext().getSelectedObject().getId());
                    boolean permission;
                    if (!Strings.isNullOrEmpty(identity.getLoginName()) && !Strings.isNullOrEmpty(getContext().getSelectedObject().getPath()) && rafPathMemberService.hasMemberInPath(identity.getLoginName(), getContext().getSelectedObject().getPath())) {
                        permission = rafPathMemberService.hasDeleteRole(identity.getLoginName(), getContext().getSelectedObject().getPath());
                    } else {
                        permission = memberService.hasDeleteRole(identity.getLoginName(), getContext().getSelectedRaf());
                    }
                    return permission && super.applicable(forCollection);
                });
            } else if (getContext().getSelectedRaf() != null) {
                return cache.get(getContext().getSelectedRaf().getId() + identity.getLoginName(), () -> {
                    LOG.debug("User: {}, Raf Code: {} . Checking delete action applicable for raf.}",
                            identity.getLoginName(),
                            getContext().getSelectedRaf().getCode());
                    Boolean permission = memberService.hasDeleteRole(identity.getLoginName(), getContext().getSelectedRaf());
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
        LOG.debug("Delete Execute");
        super.initActionModel(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected boolean finalizeAction() {

        getContext().getSeletedItems().stream()
                .filter(RafObjectUtils.distinctRafObject())
                .forEach(this::deleteObject);

        //FIXME: Burada RafEventLog çalıştırılmalı
        getContext().getSeletedItems().clear();

        return true;
    }

    public void deleteObject(RafObject o) {
        try {
            LOG.info("Raf object trying to be deleted. user: {} name : {} path: {} id: {}", identity.getLoginName(), o.getName(), o.getPath(), o.getId());
            rafService.deleteObject(o);
            deleteEvent.fire(new RafObjectDeleteEvent(o));

            //Eğer silinen şey folder ise FolderChangeEvent'ide fırlatalım ki folder ağaçları da düzenlensin
            if (o instanceof RafFolder) {
                LOG.info("Raf Folder deleted. user: {} name: {} path: {}", identity.getLoginName(), o.getName(), o.getPath());
                folderChangeEvent.fire(new RafFolderDataChangeEvent());
            }
        } catch (RafException ex) {
            //FIXME: i18n
            LOG.error("Delete Error", ex);
            FacesMessages.error("Kayıt silinemedi!");
        }
    }

}
