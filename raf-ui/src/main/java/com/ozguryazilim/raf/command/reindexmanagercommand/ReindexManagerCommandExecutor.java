package com.ozguryazilim.raf.command.reindexmanagercommand;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.telve.messagebus.command.AbstractCommandExecuter;
import com.ozguryazilim.telve.messagebus.command.CommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@CommandExecutor(command = ReindexManagerCommand.class)
public class ReindexManagerCommandExecutor extends AbstractCommandExecuter<ReindexManagerCommand> {

    private static final Logger LOG = LoggerFactory.getLogger(ReindexManagerCommandExecutor.class);

    @Inject
    RafService rafService;

    @Override
    public void execute(ReindexManagerCommand command) {
        reindex(command);
    }

    void reindex(ReindexManagerCommand command) {
        LOG.info("ReindexManagerCommand is executing. {} {}", command.getPath(), command.isAsync());
        try {
            rafService.reindex(command.getPath(), command.isAsync());
        } catch (RafException ex) {
            LOG.error("Error while reindexing.", ex);
        }
    }
}
