package com.ozguryazilim.raf.command.folderdivcommand;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.telve.messagebus.command.AbstractCommandExecuter;
import com.ozguryazilim.telve.messagebus.command.CommandExecutor;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CommandExecutor(command = FolderDivideByYearsCommand.class)
public class FolderDivideByYearsCommandExecutor extends AbstractCommandExecuter<FolderDivideByYearsCommand> {

    private static final Logger LOG = LoggerFactory.getLogger(FolderDivideByYearsCommandExecutor.class);

    @Inject
    RafService rafService;

    @Inject
    RafDefinitionService rafDefinitionService;

    @Override
    public void execute(FolderDivideByYearsCommand command) {
        try {
            if (Strings.isNullOrEmpty(command.getTargetFolder())) {
                throw new Exception("Select target folder.");
            }

            RafFolder folder = getRafFolder(command.getTargetFolder());
            
            
        } catch (Exception e) {
            LOG.error("There was an error during FolderDivideByYearsCommand", e);
        }
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

}
