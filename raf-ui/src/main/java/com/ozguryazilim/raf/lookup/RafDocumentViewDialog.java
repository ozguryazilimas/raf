package com.ozguryazilim.raf.lookup;

import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.ui.base.AbstractRafDocumentViewController;
import com.ozguryazilim.raf.ui.utils.DocumentViewDialogUtils;

import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

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

        DocumentViewDialogUtils.openDialog(getDialogName());
    }

    protected String getDialogName() {
        return "/dialogs/documentViewDialog";
    }

    public void cancelDialog() {
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
