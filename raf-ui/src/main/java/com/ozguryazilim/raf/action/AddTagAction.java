package com.ozguryazilim.raf.action;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.config.ActionPages;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;
import com.ozguryazilim.raf.utils.RafObjectUtils;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.messages.FacesMessages;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Action(dialog = ActionPages.AddTagDialog.class,
        icon = "fa-tag",
        capabilities = { ActionCapability.Ajax, ActionCapability.CollectionViews, ActionCapability.MultiSelection, ActionCapability.NeedSelection})
public class AddTagAction extends AbstractAction {

    private static final Logger LOG = LoggerFactory.getLogger(AddTagAction.class);

    @Inject
    private RafService rafService;

    @Inject
    private Identity identity;

    @Inject
    private RafMemberService memberService;

    @Inject
    private RafPathMemberService rafPathMemberService;

    private List<String> tags;
    private List<RafObject> objects;

    @Override
    public boolean applicable(boolean forCollection) {
        try {
            boolean permission;
            Optional<String> selectedObjectPath = Optional.ofNullable(getContext().getSelectedObject()).map(RafObject::getPath);
            if (selectedObjectPath.isPresent() && !Strings.isNullOrEmpty(identity.getLoginName()) && rafPathMemberService.hasMemberInPath(identity.getLoginName(), selectedObjectPath.get())) {
                permission = rafPathMemberService.hasWriteRole(identity.getLoginName(), getContext().getSelectedObject().getPath());
            } else {
                permission = memberService.hasWriteRole(identity.getLoginName(), getContext().getSelectedRaf());
            }
            return permission;
        } catch (RafException ex) {
            LOG.error("Error", ex);
            return false;
        }
    }

    @Override
    protected void initActionModel() {
        if (CollectionUtils.isEmpty(getContext().getSeletedItems())) {
            FacesMessages.error("Selected items cannot be empty");
            return;
        }

        objects = getContext().getSeletedItems().stream()
                .filter(RafObjectUtils.distinctRafObject())
                .collect(Collectors.toList());

        tags = new ArrayList<>();
    }

    @Override
    protected boolean finalizeAction() {
        //Update objects tags and save
        objects.forEach(obj -> {
            try {
                List<String> updatedTagList = Stream.concat(obj.getTags().stream(), tags.stream())
                        .distinct()
                        .collect(Collectors.toList());

                obj.setTags(updatedTagList);
                rafService.saveProperties(obj);
            } catch (RafException e) {
                FacesMessages.error("Error while updating tags");
            }
        });
        resetActionInitialStates();
        return super.finalizeAction();
    }

    @Override
    public void cancelDialog() {
        resetActionInitialStates();
        super.cancelDialog();
    }

    private void resetActionInitialStates() {
        tags = new ArrayList<>();
        objects = new ArrayList<>();
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<RafObject> getObjects() {
        return objects;
    }

    public void setObjects(List<RafObject> objects) {
        this.objects = objects;
    }
}
