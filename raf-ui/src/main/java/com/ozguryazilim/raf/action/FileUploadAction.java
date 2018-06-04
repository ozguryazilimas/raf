/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.raf.action;

import com.ozguryazilim.raf.RafContext;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.events.RafChangedEvent;
import com.ozguryazilim.telve.messages.FacesMessages;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@SessionScoped
@Named
public class FileUploadAction implements Serializable{
    
    private static final Logger LOG = LoggerFactory.getLogger(FileUploadAction.class);
    
    @Inject
    private RafContext context;
    
    @Inject
    private RafService rafService;
    
    @Inject
    private Event<RafChangedEvent> rafChangedEvent;
    
    
    
    public void execute(){
        
        Map<String, Object> options = new HashMap<>();

        RequestContext.getCurrentInstance().openDialog("/actions/fileUploadDialog", options, null);
    }
    
    public void closeDialog() {
        
        //document.setPath( context.getCollection().getPath() + "/" + document.getName());
        
        //String filePath = context.getCollection().getPath() + "/" + document.getName();
        
        /*
        try {
            //rafService.createFolder(folder);
        } catch (RafException ex) {
            //TODO: i18n
            FacesMessages.error("Raf Tanımlaması Yapılamadı", ex.getMessage());
        }
        */
        
        rafChangedEvent.fire(new RafChangedEvent());
        
        RequestContext.getCurrentInstance().closeDialog(null);
    }
    
    public void cancelDialog() {
        RequestContext.getCurrentInstance().closeDialog(null);
    }

    public void handleFileUpload(FileUploadEvent event) {
        LOG.info("Uploaded File : {}", event.getFile().getFileName());

        String fileNamePath = event.getFile().getFileName();
        String fileName = fileNamePath.substring(fileNamePath.lastIndexOf(File.separatorChar) + 1);

        
        try {

            String folderName = context.getCollection().getPath();
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
