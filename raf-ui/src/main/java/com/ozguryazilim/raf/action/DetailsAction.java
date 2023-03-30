package com.ozguryazilim.raf.action;

import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.config.ActionPages;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;
import com.ozguryazilim.telve.messages.FacesMessages;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Action(dialog = ActionPages.DetailsDialog.class,
        icon = "fa-info-circle",
        capabilities = {ActionCapability.Ajax, ActionCapability.CollectionViews, ActionCapability.MultiSelection, ActionCapability.NeedSelection})
public class DetailsAction extends AbstractAction {
    private static final Logger LOG = LoggerFactory.getLogger(DetailsAction.class);

    @Inject
    private RafService rafService;

    private List<RafObject> selectedItems;
    private long totalSize;

    @Override
    protected void initActionModel() {
        if (CollectionUtils.isEmpty(getContext().getSeletedItems())) {
            FacesMessages.error("Selected items cannot be empty");
            return;
        }

        String objectNames = getContext().getSeletedItems().stream()
                .map(RafObject::getName)
                .collect(Collectors.joining(","));
        LOG.info("Raf Object Details are prepearing for objects: {}", objectNames);

        selectedItems = getContext().getSeletedItems();
        totalSize = rafService.getRafObjectsSize(getContext().getSeletedItems());
    }

    @Override
    protected boolean finalizeAction() {
        //Update objects tags and save
        resetActionInitialStates();
        return super.finalizeAction();
    }

    @Override
    public void cancelDialog() {
        resetActionInitialStates();
        super.cancelDialog();
    }

    @Override
    protected void initDialogOptions(Map<String, Object> options) {
        options.put("contentHeight", 70);
        options.put("contentWidth", 320);
        options.put("closeable", false);
    }

    private void resetActionInitialStates() {
        selectedItems = null;
        totalSize = 0;
    }

    public List<RafObject> getSelectedItems() {
        return selectedItems;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public Map<String, Long> getSelectedContentCounts() {
        if (selectedItems != null && !selectedItems.isEmpty()) {
            return selectedItems.stream().collect(Collectors.groupingBy(obj -> obj.getClass().getSimpleName(), Collectors.counting()));
        }
        return Collections.emptyMap();
    }

}
