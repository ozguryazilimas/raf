package com.ozguryazilim.raf.action;

import com.ozguryazilim.raf.RafController;
import com.ozguryazilim.raf.events.RafFolderDataChangeEvent;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.telve.auth.Identity;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Klasör içerisinde sonraki sayfayı listeler.
 *
 * @author oyas
 */
//@Action(icon = "fa-arrow-right",
//        capabilities = {ActionCapability.Ajax, ActionCapability.CollectionViews},
//        includedMimeType = "raf/folder",
//        order = 9)
//scrollview ile lazy load özelliği eklendiği için bu actiona gerek kalmadı.
public class NextPageAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(NextPageAction.class);

    @Inject
    private RafController rafController;

    @Inject
    private Event<RafFolderDataChangeEvent> nextPageEvent;

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
        rafController.nextPage();
        nextPageEvent.fire(new RafFolderDataChangeEvent());
        return true;
    }

    public RafFolder getFolder() {
        return folder;
    }

    public void setFolder(RafFolder folder) {
        this.folder = folder;
    }

    @Override
    public boolean isEnabled() {
        //scrollview ile lazy load özelliği eklendiği için bu actiona gerek kalmadı.
        return false;
    }
}
