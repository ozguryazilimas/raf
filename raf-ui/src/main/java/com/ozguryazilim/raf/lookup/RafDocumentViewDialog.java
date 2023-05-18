package com.ozguryazilim.raf.lookup;

import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.ui.base.AbstractRafDocumentViewController;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import org.primefaces.context.RequestContext;

/**
 *
 * @author oyas
 */
@SessionScoped
@Named
public class RafDocumentViewDialog extends AbstractRafDocumentViewController implements Serializable {
    private boolean readerPageState = Boolean.FALSE;

    public void openDialog(RafDocument object) {
        setReaderPageState(false);
        setObject(object);

        Map<String, Object> options = new HashMap<>();
        options.put("draggable", false);
        options.put("resizable", false);
        options.put("modal", true);

        RequestContext.getCurrentInstance().openDialog(getDialogName(), options, null);
    }

    public String getUniqueFormName() {
        return UUID.randomUUID().toString();
    }

    protected String getDialogName() {
        //String viewId = getDialogPageViewId();
        //return viewId.substring(0, viewId.indexOf(".xhtml"));
        return "/dialogs/documentViewDialog";
    }

    /**
     * Dialogu hiç bir şey seçmeden kapatır.
     */
    public void cancelDialog() {
        RequestContext.getCurrentInstance().closeDialog(null);
        setReaderPageState(false);
    }

    public boolean getSupportBreadcrumb() {
        return false;
    }

    public boolean getReaderPageState() {
        return readerPageState;
    }

    public void setReaderPageState(boolean val) {
        this.readerPageState = val;
    }
}
