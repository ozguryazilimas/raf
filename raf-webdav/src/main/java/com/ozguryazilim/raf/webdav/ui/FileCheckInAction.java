package com.ozguryazilim.raf.webdav.ui;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.messages.FacesMessages;
import java.util.Date;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Action(icon = "fa-unlock",
        capabilities = {ActionCapability.DetailViews},
        excludeMimeType = "raf/folder")
public class FileCheckInAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(FileCheckInAction.class);

    @Inject
    Identity identity;

    @Inject
    RafService rafService;

    @Override
    public boolean isEnabled() {
        try {//sadece checkout yapan kullanıcı veya admin kullanıcı checkin yapabilir.
            return rafService.getRafCheckStatus(getContext().getSelectedObject().getPath()) && (identity.isPermitted("admin") || identity.getUserName().equals(rafService.getRafCheckerUser(getContext().getSelectedObject().getPath())));
        } catch (RafException ex) {
            LOG.error("Raf Exception", ex);
        }
        return super.isEnabled(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected boolean finalizeAction() {
        RafObject rafObject = getContext().getSelectedObject();
        try {
            if (!rafService.getRafCheckStatus(rafObject.getPath())) {
                FacesMessages.error("Dosyanın yazma kilidi zaten açık.");//FIXME : i118
            } else {
                rafService.setRafCheckOutValue(rafObject.getPath(), Boolean.FALSE, identity.getUserName(), new Date());
                FacesMessages.info("Dosya kilidi açıldı.");//FIXME : i118
            }
        } catch (RafException ex) {
            LOG.error("Raf Exception", ex);
        }
        return super.finalizeAction();
    }
}
