package com.ozguryazilim.raf.action;

import com.ozguryazilim.raf.RafController;
import com.ozguryazilim.raf.events.RafFolderChangeEvent;
import com.ozguryazilim.raf.events.RafFolderDataChangeEvent;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;
import com.ozguryazilim.telve.auth.Identity;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Klasör içerisinde önceki sayfayı listeler.
 *
 * @author oyas
 */
@Action(icon = "fa-arrow-left",
        capabilities = {ActionCapability.Ajax, ActionCapability.CollectionViews},
        includedMimeType = "raf/folder",
        order = 8)
public class PreviousPageAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(PreviousPageAction.class);

    @Inject
    private RafController rafController;

    @Inject
    private Event<RafFolderDataChangeEvent> previousPageEvent;

    @Inject
    private Identity identity;

    @Inject
    private RafMemberService memberService;

    @Inject
    private RafPathMemberService rafPathMemberService;

    private RafFolder folder;

    @Override
    protected void initActionModel() {

    }

    @Override
    protected boolean finalizeAction() {
        rafController.previousPage();
        previousPageEvent.fire(new RafFolderDataChangeEvent());

        return true;
    }

    public RafFolder getFolder() {
        return folder;
    }

    public void setFolder(RafFolder folder) {
        this.folder = folder;
    }

}
