package com.ozguryazilim.raf.command.checkmissingcontentscommand;

import com.ozguryazilim.telve.messagebus.command.AbstractCommandExecuter;
import com.ozguryazilim.telve.messagebus.command.CommandExecutor;
import me.desair.tus.server.TusFileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.NoSuchFileException;

@CommandExecutor(command = CheckMissingContentsCommand.class)
public class CheckMissingContentsCommandExecutor extends AbstractCommandExecuter<CheckMissingContentsCommand> {

    private static final Logger LOG = LoggerFactory.getLogger(CheckMissingContentsCommandExecutor.class);

    @Inject
    private TusFileUploadService fileUploadService;

    @Override
    public void execute(CheckMissingContentsCommand command) {
        try {
            fileUploadService.cleanup();
            LOG.info("Cleaned up expired uploads and locked stales.");
        }
        catch (NoSuchFileException e) {
            LOG.error("Could not cleanup expired uploads and locked stales. {} not found.", e.getMessage());
        }
        catch (IOException e) {
            LOG.error("Error while cleaning expired uploads", e);
        }
    }

}
