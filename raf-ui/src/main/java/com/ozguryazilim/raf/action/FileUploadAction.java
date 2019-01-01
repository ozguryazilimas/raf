package com.ozguryazilim.raf.action;

import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.config.ActionPages;
import com.ozguryazilim.raf.events.RafUploadEvent;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;
import java.util.Map;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@Action(dialog = ActionPages.FileUploadDialog.class, 
        icon = "fa-upload",
        capabilities = {ActionCapability.Ajax, ActionCapability.CollectionViews},
        includedMimeType = "raf/folder",
        order = 0)
public class FileUploadAction extends AbstractAction{
    
    private static final Logger LOG = LoggerFactory.getLogger(FileUploadAction.class);
    
    @Inject
    private RafService rafService;
    
    @Inject
    private RafContext rafContext;
    
    @Inject
    private Event<RafUploadEvent> rafUploadEvent;

    private String rafCode;
    private String uploadPath;
    private boolean actionExec = Boolean.TRUE;

    @Override
    protected void initActionModel() {
        super.initActionModel();
        
        rafCode = rafContext.getSelectedRaf().getCode();
        uploadPath = rafContext.getCollection().getPath();
        actionExec = Boolean.TRUE;
    }
    
    @Override
    protected void initDialogOptions(Map<String, Object> options) {
        options.put("contentHeight", 480);
        options.put("contentWidth", 640);
    }

    @Override
    protected boolean finalizeAction() {
        if( actionExec ){
            //Eğer action düğmesinden çağrılmış ise normal UploadEventi. Böylece RafController yakalar.
            //FIXME: doğru eventi fırlatalım.
            rafUploadEvent.fire(new RafUploadEvent());
        }
        
        return super.finalizeAction(); 
    }

    /**
     * Normal akış dışında bir yerden Upload çalıştırmak için kullanılır.
     * 
     * Bakınız DocumentsWidgetController implementasyonları. TaskController
     * 
     * @param rafCode
     * @param uploadPath 
     */
    public void execute( String rafCode, String uploadPath ){
        this.rafCode = rafCode;
        this.uploadPath = uploadPath;
        actionExec = Boolean.FALSE;
        openDialog();
    }
    
    public String getRafCode() {
        return rafCode;
    }

    public void setRafCode(String rafCode) {
        this.rafCode = rafCode;
    }

    public String getUploadPath() {
        return uploadPath;
    }

    public void setUploadPath(String uploadPath) {
        this.uploadPath = uploadPath;
    }
}
