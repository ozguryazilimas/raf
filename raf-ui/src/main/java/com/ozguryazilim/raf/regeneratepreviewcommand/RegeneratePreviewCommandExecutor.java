package com.ozguryazilim.raf.regeneratepreviewcommand;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.definition.RafDefinitionService;
import com.ozguryazilim.raf.encoder.RafEncoder;
import com.ozguryazilim.raf.encoder.RafEncoderFactory;
import com.ozguryazilim.raf.entities.RafDefinition;
import com.ozguryazilim.raf.models.RafCollection;
import com.ozguryazilim.raf.models.RafDocument;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.raf.models.RafObject;
import com.ozguryazilim.telve.messagebus.command.AbstractCommandExecuter;
import com.ozguryazilim.telve.messagebus.command.CommandExecutor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CommandExecutor(command = RegeneratePreviewCommand.class)
public class RegeneratePreviewCommandExecutor extends AbstractCommandExecuter<RegeneratePreviewCommand> {

    private static final Logger LOG = LoggerFactory.getLogger(RegeneratePreviewCommandExecutor.class);

    @Inject
    RafService rafService;

    @Inject
    RafDefinitionService rafDefinitionService;

    @Override
    public void execute(RegeneratePreviewCommand command) {
        try {
            if (Strings.isNullOrEmpty(command.getRafPath())) {
                LOG.error("Regenerate preview importer command configuration is not valid!");
                return;
            }
            String rafPath = command.getRafPath();

            if ("/".equals(rafPath.substring(rafPath.length() - 1, rafPath.length()))) {
                rafPath = rafPath.substring(0, rafPath.lastIndexOf("/"));
            }

            generatePreviews(command);
        } catch (Exception e) {
            LOG.error("There was an error during RegeneratePreviewCommand", e);
        }
    }

    public void reGenerateObjectPreviews(List<RafObject> rafObjects) throws RafException {
        for (RafObject rafObject : rafObjects) {
            LOG.debug("RegeneratePreview is executing. {}", rafObject.getPath());
            if (rafObject instanceof RafDocument && ((RafDocument) rafObject).getHasPreview()) {
                rafService.reGeneratePreview(rafObject.getId());
            } else if (rafObject instanceof RafFolder) {
                RafCollection r = rafService.getCollection(rafObject.getId());
                reGenerateObjectPreviews(r.getItems());
            }
        }
    }

    void generatePreviews(RegeneratePreviewCommand command) {
        LOG.info("RegeneratePreviewCommand is executing. {}", command.getRafPath());
        RafObject rafObjet;
        try {
            rafObjet = rafService.getRafObjectByPath(command.getRafPath());
            RafCollection rafCollection = rafService.getCollection(rafObjet.getId());
            reGenerateObjectPreviews(rafCollection.getItems());
        } catch (RafException ex) {
            LOG.error("Path not found : {}", command.getRafPath(), ex);
        }

        LOG.info("RegeneratePreviewCommand is executed.");
    }

}
