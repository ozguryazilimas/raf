package com.ozguryazilim.raf.wopi;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;
import com.ozguryazilim.raf.wopi.config.WopiPages;
import com.ozguryazilim.telve.auth.Identity;

import javax.inject.Inject;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.config.view.navigation.ViewNavigationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hakan
 */
@Action(
        icon = "fa-edit",
        capabilities = {ActionCapability.CollectionViews, ActionCapability.DetailViews}
//,
//includedMimeType = "application/zip"
)
public class EditOnlineAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(EditOnlineAction.class);

    @Inject
    private ViewNavigationHandler viewNavigationHandler;

    @Inject
    private OnlineEditorController onlineEditorController;

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

            if ("true".equals(ConfigResolver.getPropertyValue("raf.wopi.active", "false"))) {
                if (getContext().getSelectedObject() != null && !Strings.isNullOrEmpty(identity.getLoginName()) && !Strings.isNullOrEmpty(getContext().getSelectedObject().getPath()) && rafPathMemberService.hasMemberInPath(identity.getLoginName(), getContext().getSelectedObject().getPath())) {
                    permission = rafPathMemberService.hasWriteRole(identity.getLoginName(), getContext().getSelectedObject().getPath());
                } else {
                    permission = memberService.hasWriteRole(identity.getLoginName(), getContext().getSelectedRaf());
                }
            }

            return permission && super.applicable(forCollection);
        } catch (RafException ex) {
            LOG.error("Error", ex);
            return super.applicable(forCollection);
        }
    }

    @Override
    protected boolean finalizeAction() {
        return super.finalizeAction();
    }

    @Override
    public boolean hasDialog() {
        return true;
    }

    @Override
    protected void openDialog() {
        //TODO: Burada NPE kontrolü yapmakta fayda var.
        onlineEditorController.openEditor(getContext().getSelectedObject().getId());
    }

}
