package com.ozguryazilim.raf.command.cleanupexpireduploadscommand;

import com.ozguryazilim.telve.messagebus.command.AbstractCommandExecuter;
import com.ozguryazilim.telve.messagebus.command.CommandExecutor;
import me.desair.tus.server.TusFileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.NoSuchFileException;

@CommandExecutor(command = CleanupExpiredUploadsCommand.class)
public class CleanupExpiredUploadsCommandExecutor extends AbstractCommandExecuter<CleanupExpiredUploadsCommand> {

    private static final Logger LOG = LoggerFactory.getLogger(CleanupExpiredUploadsCommandExecutor.class);

    @Inject
    private TusFileUploadService fileUploadService;

    @Override
    public void execute(CleanupExpiredUploadsCommand command) {
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
