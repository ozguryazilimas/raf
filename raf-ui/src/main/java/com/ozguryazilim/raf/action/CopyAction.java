package com.ozguryazilim.raf.action;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;
import com.ozguryazilim.telve.auth.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author oyas
 */
@Action(icon = "fa-copy",
        capabilities = {ActionCapability.Ajax, ActionCapability.CollectionViews, ActionCapability.DetailViews, ActionCapability.MultiSelection, ActionCapability.NeedSelection},
        group = 10,
        actionPermission = "insert",
        order = 1
)
public class CopyAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(CopyAction.class);

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
                        permission = rafPathMemberService.hasReadRole(identity.getLoginName(), getContext().getSelectedObject().getPath());
                    } else {
                        permission = memberService.hasReadRole(identity.getLoginName(), getContext().getSelectedRaf());
                    }
                    return permission && super.applicable(forCollection);
                });
            } else if(getContext().getSelectedRaf() != null) {
                return cache.get(getContext().getSelectedRaf().getId() + identity.getLoginName(), () -> {
                    boolean permission = memberService.hasReadRole(identity.getLoginName(), getContext().getSelectedRaf());
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

        getContext().getClipboard().clear();
        getContext().getClipboard().addAll(getContext().getSeletedItems());
        getContext().setClipboardAction(this);

        return super.finalizeAction();
    }

}
