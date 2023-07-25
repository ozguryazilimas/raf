package com.ozguryazilim.raf.ui.base.metadatapanels;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.category.RafCategoryService;
import com.ozguryazilim.raf.config.MetadataPanelPages;
import com.ozguryazilim.raf.entities.RafCategory;
import com.ozguryazilim.raf.events.EventLogCommand;
import com.ozguryazilim.raf.events.EventLogCommandBuilder;
import com.ozguryazilim.raf.member.RafMemberService;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.objet.member.RafPathMemberService;
import com.ozguryazilim.raf.ui.base.AbstractMetadataPanel;
import com.ozguryazilim.raf.ui.base.MetadataPanel;
import com.ozguryazilim.telve.auth.Identity;
import com.ozguryazilim.telve.messagebus.command.CommandSender;
import com.ozguryazilim.telve.messages.FacesMessages;

import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Her hangi bir belge için temel bilgiler.
 *
 * RafObject üzerinden alınır.
 *
 * FIXME: edit süreci ile ilgili yapılacak şeyler var.
 *
 * @author Hakan Uygun
 */
@MetadataPanel(type = "nt:file", view = MetadataPanelPages.BasicMetadataPanel.class, editor = MetadataPanelPages.BasicMetadataEditorDialog.class, order = 0)
public class BasicMetadataPanel extends AbstractMetadataPanel{

    private static final Logger LOG = LoggerFactory.getLogger(BasicMetadataPanel.class);

    @Inject
    private RafService rafService;

    @Inject
    private RafCategoryService categoryService;

    @Inject
    private Identity identity;

    @Inject
    private RafMemberService memberService;

    @Inject
    private RafPathMemberService rafPathMemberService;

    @Inject
    private CommandSender commandSender;

    private RafCategory category;

    private RafObject object;

    public RafObject getObject() {
        return object;
    }

    public void setObject(RafObject object) {
        this.object = object;
    }

    @Override
    protected void initEditModel() {
        //FIXME: Burada servisten nesneyi bulacağız.
        category = null;
        if( getObject().getCategoryId() != null ){
            category = categoryService.findById(getObject().getCategoryId());
        }
    }

    @Override
    protected void save() {
        try {
            //Check if title changed
            String oldTitle = rafService.getRafObject(getObject().getId()).getTitle();
            if (!getObject().getTitle().equals(oldTitle)) {
                //Send event command
                EventLogCommand command = EventLogCommandBuilder.forRaf(getContext().getSelectedRaf().getCode())
                        .eventType("MetadataChangeEvent.Title")
                        .forRafObject(object)
                        .message("event." + "MetadataChangeEvent.Title" + "$%&" + (identity != null ? identity.getUserName() : "Sistem") + "$%&" + oldTitle + "$%&" + getObject().getTitle())
                        .user(identity != null ? identity.getLoginName() : "SYSTEM")
                        .build();

                commandSender.sendCommand(command);
            }

            LOG.info("Selected Category : {}", category);
            if( category != null ){
                getObject().setCategory(category.getName());
                getObject().setCategoryPath(category.getPath());
                getObject().setCategoryId(category.getId());
                LOG.info("Selected Category : {}", category.getName());
                //TODO: category attribute atanacak
            } else {
                //Modelde category varsa boşaltılacak
                getObject().setCategory(null);
                getObject().setCategoryPath(null);
                getObject().setCategoryId(null);
            }
            //FIXME: yetki kontrolü nerede yapılacak?
            rafService.saveProperties(getObject());
        } catch (RafException ex) {
            //FIXME: i18n
            LOG.error("Properties cannot saved", ex);
            FacesMessages.error("Properties cannot saved");
        }
    }

    @Override
    public void closeDialog() {

        if (getObject().getTitle() == null || getObject().getTitle().isEmpty()) {
            FacesMessages.error("Object name can not be empty");
        } else {
            save();
            RequestContext.getCurrentInstance().closeDialog(null);
        }
    }

    @Override
    public boolean canEdit() {
        try {
            boolean permission;
            Optional<String> selectedObjectPath = Optional.ofNullable(getContext().getSelectedObject()).map(RafObject::getPath);
            if (selectedObjectPath.isPresent() && !Strings.isNullOrEmpty(identity.getLoginName()) && rafPathMemberService.hasMemberInPath(identity.getLoginName(), selectedObjectPath.get())) {
                permission = rafPathMemberService.hasWriteRole(identity.getLoginName(), selectedObjectPath.get());
            } else {
                permission = memberService.hasWriteRole(identity.getLoginName(), getContext().getSelectedRaf());
            }
            return permission;
        } catch (RafException ex) {
            LOG.error("Error", ex);
            return false;
        }
    }

    public RafCategory getCategory() {
        return category;
    }

    public void setCategory(RafCategory category) {
        this.category = category;
    }

}
