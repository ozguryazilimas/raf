package com.ozguryazilim.raf.action;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;
import com.ozguryazilim.telve.auth.Identity;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@Action(icon = "fa-copy",
        capabilities = {ActionCapability.Ajax, ActionCapability.CollectionViews, ActionCapability.DetailViews, ActionCapability.MultiSelection, ActionCapability.NeedSelection},
        group = 10,
        order = 1
)
public class CopyAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(CopyAction.class);

    @Inject
    private Identity identity;

    @Inject
    private RafMemberService memberService;

    @Override
    public boolean applicable(boolean forCollection) {
        try {
            boolean hasRafRole = getContext().getSelectedRaf().getId() > 0 && (memberService.hasMemberRole(identity.getLoginName(), "MANAGER", getContext().getSelectedRaf())
                    || memberService.hasMemberRole(identity.getLoginName(), "CONTRIBUTER", getContext().getSelectedRaf())
                    || memberService.hasMemberRole(identity.getLoginName(), "EDITOR", getContext().getSelectedRaf()));
            return hasRafRole && super.applicable(forCollection);
        } catch (RafException ex) {
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
