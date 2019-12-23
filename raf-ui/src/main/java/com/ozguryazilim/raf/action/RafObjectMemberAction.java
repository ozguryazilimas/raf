package com.ozguryazilim.raf.action;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.config.ActionPages;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
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
@Action(icon = "fa-users",
        dialog = ActionPages.RafObjectMembers.class,
        capabilities = {ActionCapability.Ajax, ActionCapability.CollectionViews, ActionCapability.DetailViews},
        order = 1,
        excludeMimeType = "raf/rafNode"
)
public class RafObjectMemberAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(RafObjectMemberAction.class);

    @Inject
    private Identity identity;

    @Inject
    private RafMemberService memberService;

    @Inject
    private RafPathMemberService rafObjectMemberService;

    @Inject
    private RafObjectMemberController rafObjectMemberController;

    @Override
    public boolean applicable(boolean forCollection) {
        try {
            boolean hasRafRole = getContext().getSelectedRaf().getId() > 0 && memberService.hasManagerRole(identity.getLoginName(), getContext().getSelectedRaf());

            boolean hasRafObjectRole = getContext().getSelectedObject() != null && rafObjectMemberService.hasManagerRole(identity.getLoginName(), getContext().getSelectedObject().getPath());
            //Eğer nesne üyeliği yok ise raf yetkisi baz alınır, eğer nesne üyeliği var ise nesne yetkisi baz alınır.            
            return getContext().getSelectedObject() != null && getContext().getSelectedObject().getPath().split("/").length > 3 && ((!rafObjectMemberService.isMemberOf(identity.getLoginName(), getContext().getSelectedObject().getPath()) && hasRafRole) || hasRafObjectRole) && super.applicable(forCollection);
        } catch (RafException ex) {
            LOG.error("Error", ex);
            return super.applicable(forCollection);
        }
    }

    @Override
    protected void initActionModel() {
        rafObjectMemberController.setPath(getContext().getSelectedObject().getPath());
        rafObjectMemberController.init();
    }

    @Override
    protected boolean finalizeAction() {
        return super.finalizeAction();
    }

}
