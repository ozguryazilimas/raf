package com.ozguryazilim.raf.action;

import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;
import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

@Action(icon = "fa-eye",
        capabilities = {ActionCapability.Ajax, ActionCapability.DetailViews},
        order = 0
)
public class ShowDirectionAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(ShowDirectionAction.class);

    @Inject
    private RafService rafService;

    @Inject
    private RafContext rafContext;

    @Override
    public boolean applicable(boolean forCollection) {
        return true;
    }

    @Override
    protected boolean finalizeAction() {

        RafObject rafObject = rafContext.getSelectedObject();
        List<String> parents = new ArrayList<>();
        boolean flag = true;
        try {

            do {
                rafObject = rafService.getRafObject(rafObject.getParentId());
                if (!rafObject.getPath().equals("/RAF")) {
                    parents.add(rafObject.getId());
                }else {
                    flag = false;
                }
            } while (flag);
        } catch (RafException e) {
            e.printStackTrace();
        }
        Collections.reverse(parents);
        String script = "openSelectedNodes('"+String.join("','",parents)+"');";
        PrimeFaces.current().executeScript(script);
        return true;
    }

}
