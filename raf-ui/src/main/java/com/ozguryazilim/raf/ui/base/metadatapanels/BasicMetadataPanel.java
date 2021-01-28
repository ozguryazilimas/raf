package com.ozguryazilim.raf.ui.base.metadatapanels;

import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.category.RafCategoryService;
import com.ozguryazilim.raf.config.MetadataPanelPages;
import com.ozguryazilim.raf.entities.RafCategory;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.raf.ui.base.AbstractMetadataPanel;
import com.ozguryazilim.raf.ui.base.MetadataPanel;
import com.ozguryazilim.telve.messages.FacesMessages;

import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private RafContext context;

    @Inject
    private RafCategoryService categoryService;

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

    public RafCategory getCategory() {
        return category;
    }

    public void setCategory(RafCategory category) {
        this.category = category;
    }

}
