package com.ozguryazilim.raf.share;

import com.ozguryazilim.telve.messagebus.command.AbstractCommandExecuter;
import com.ozguryazilim.telve.messagebus.command.CommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@CommandExecutor(command = CleanupOutdatedSharingsCommand.class)
public class CleanupOutdatedSharingsCommandExecutor extends AbstractCommandExecuter<CleanupOutdatedSharingsCommand> {

    private static final Logger LOG = LoggerFactory.getLogger(CleanupOutdatedSharingsCommandExecutor.class);

    @Inject
    private RafShareService rafShareService;

    @Override
    public void execute(CleanupOutdatedSharingsCommand command) {
        LOG.info("Cleanup outdated sharings job starting...");
        rafShareService.clearOutdated();
        LOG.info("Cleanup outdated sharings job finished.");
    }
}
