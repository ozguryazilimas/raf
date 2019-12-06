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

@Action(icon = "fa-lock",
        capabilities = {ActionCapability.DetailViews},
        excludeMimeType = "raf/folder")
public class FileCheckOutAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(FileCheckOutAction.class);

    @Inject
    Identity identity;

    @Inject
    RafService rafService;

    @Override
    public boolean isEnabled() {
        try {
            return !rafService.getRafCheckStatus(getContext().getSelectedObject().getPath());
        } catch (RafException ex) {
            LOG.error("Raf Exception", ex);
        }
        return super.isEnabled(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected boolean finalizeAction() {
        RafObject rafObject = getContext().getSelectedObject();
        try {
            if (rafService.getRafCheckStatus(rafObject.getPath())) {
                FacesMessages.error("Dosya başka bir kullanıcı tarafından kilitlenmiş.", String.format("Kilitleyen kullanıcı : %s", rafService.getRafCheckerUser(rafObject.getPath()))); //FIXME : i118
            } else {
                rafService.checkout(rafObject.getPath());//dosya uygulamaya alındı ise dışarıda yazmaya kapalı olmalı bu yüzden checkin yapılıp read only false getiriliyor.
                rafService.setRafCheckOutValue(rafObject.getPath(), Boolean.TRUE, identity.getUserName(), new Date());
                FacesMessages.info("Dosya kilitlendi."); //FIXME : i118
            }
        } catch (RafException ex) {
            LOG.error("Raf Exception", ex);
        }
        return super.finalizeAction();
    }
}
