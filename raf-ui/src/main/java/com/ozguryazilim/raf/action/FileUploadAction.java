/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.action;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.config.ActionPages;
import com.ozguryazilim.raf.events.RafUploadEvent;
import com.ozguryazilim.raf.ui.base.AbstractAction;
import com.ozguryazilim.raf.ui.base.Action;
import com.ozguryazilim.raf.ui.base.ActionCapability;
import com.ozguryazilim.telve.messages.FacesMessages;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import org.primefaces.event.FileUploadEvent;
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
    private Event<RafUploadEvent> rafUploadEvent;

    @Override
    protected void initDialogOptions(Map<String, Object> options) {
        options.put("contentHeight", 480);
        options.put("contentWidth", 640);
    }

    @Override
    protected boolean finalizeAction() {
        //FIXME: doğru eventi fırlatalım.
        rafUploadEvent.fire(new RafUploadEvent());
        return super.finalizeAction(); 
    }
    
    
    public void handleFileUpload(FileUploadEvent event) {
        LOG.info("Uploaded File : {}", event.getFile().getFileName());

        String fileNamePath = event.getFile().getFileName();
        String fileName = fileNamePath.substring(fileNamePath.lastIndexOf(File.separatorChar) + 1);

        
        try {

            String folderName = getContext().getCollection().getPath();
            String path = folderName + "/" + fileName;

            LOG.info("File Path : {}", path);
            rafService.uploadDocument(path, event.getFile().getInputstream());

        } catch (IOException e) {
            LOG.error("IO Error", e);
        } catch (RafException ex) {
            LOG.error("File connot upload", ex);
            //FIXME: i18n
            FacesMessages.error("File can not uploaded!");
        }
        
        
    }
}
