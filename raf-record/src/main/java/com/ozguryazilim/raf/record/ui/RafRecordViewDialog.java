package com.ozguryazilim.raf.record.ui;

import com.ozguryazilim.raf.models.RafRecord;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import com.ozguryazilim.raf.ui.utils.DocumentViewDialogUtils;

/**
 *
 * @author oyas
 */
@SessionScoped
@Named
public class RafRecordViewDialog extends AbstractRafRecordViewController implements Serializable{

    public void openDialog( RafRecord object ) {
        setObject(object);
        DocumentViewDialogUtils.openDialog(getDialogName());
    }
    
    protected String getDialogName() {
        return "/record/recordViewDialog";
    }
    
    /**
     * Dialogu hiç bir şey seçmeden kapatır.
     */
    public void cancelDialog() {
        DocumentViewDialogUtils.cancelDialog();
    }
    
    public boolean getSupportBreadcrumb() {
        return false;
    }
}
