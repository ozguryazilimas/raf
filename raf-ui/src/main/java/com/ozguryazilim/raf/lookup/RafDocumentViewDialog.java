package com.ozguryazilim.raf.lookup;

import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.ui.base.AbstractRafDocumentViewController;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import org.primefaces.context.RequestContext;

/**
 *
 * @author oyas
 */
@SessionScoped
@Named
public class RafDocumentViewDialog extends AbstractRafDocumentViewController implements Serializable{
    
    public void openDialog( RafDocument object ) {
        setObject(object);
        
        Map<String, Object> options = new HashMap<>();

        RequestContext.getCurrentInstance().openDialog(getDialogName(), options, null);
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
    }
    
    public boolean getSupportBreadcrumb() {
        return false;
    }
}
