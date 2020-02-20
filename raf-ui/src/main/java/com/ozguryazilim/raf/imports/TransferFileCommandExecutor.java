/*
 * 
 * 
 * 
 */
package com.ozguryazilim.raf.imports;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.telve.messagebus.command.AbstractCommandExecuter;
import com.ozguryazilim.telve.messagebus.command.CommandExecutor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author zzz mailto:zzz
 */
@CommandExecutor(command = TransferFileCommand.class)
public class TransferFileCommandExecutor extends AbstractCommandExecuter<TransferFileCommand> {

    private static final Logger LOG = LoggerFactory.getLogger(TransferFileCommandExecutor.class);

    @Inject
    RafService rafService;

    @Inject
    RafDefinitionService rafDefinitionService;

    @Override
    public void execute(TransferFileCommand c) {
        transferFile(c.getRafFilePath(), c.getFileName(), c.getRafFolder(), c.getFile());
    }

    private RafObject getRafObject(String rafFilePath) {
        try {
            RafObject ro = rafService.getRafObjectByPath(rafFilePath);
            return ro;
        } catch (RafException ex) {
            LOG.debug("Raf exception", ex);
            return null;
        }
    }

    private RafFolder getRafFolder(String rafFolder) {
        try {
            RafFolder rf = rafService.getFolder(rafFolder);
            return rf;
        } catch (RafException ex) {
            LOG.debug("Raf exception", ex);
            return null;
        }
    }

    private void transferFile(String rafFilePath, String fileName, String rafFolder, File file) {
        try {
            LOG.debug("Checking RAF folder {}", rafFolder);
            RafFolder rf = getRafFolder(rafFolder);
            LOG.debug("Checking status : {}", rf != null);
            if (rf == null) {
                LOG.debug("Raf folder is creating");
                rf = rafService.createFolder(rafFolder);
            }
            LOG.debug("File is transfering {}", rafFilePath);
            RafDocument doc = rafService.uploadDocument(rafFilePath, new FileInputStream(file));
        } catch (RafException ex) {
            LOG.error("Raf exception", ex);
        } catch (FileNotFoundException ex) {
            LOG.error("File Not Found exception", ex);
        }
    }
}
