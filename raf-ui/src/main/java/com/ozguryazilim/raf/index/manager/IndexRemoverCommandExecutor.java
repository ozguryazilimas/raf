package com.ozguryazilim.raf.index.manager;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.telve.messagebus.command.AbstractCommandExecuter;
import com.ozguryazilim.telve.messagebus.command.CommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Arrays;

@CommandExecutor(command = IndexRemoverCommand.class)
public class IndexRemoverCommandExecutor extends AbstractCommandExecuter<IndexRemoverCommand> {

    private static final Logger LOG = LoggerFactory.getLogger(IndexRemoverCommandExecutor.class);

    @Inject
    RafService rafService;

    IndexRemoverCommand command;

    @Override
    public void execute(IndexRemoverCommand command) {
        this.command = command;
        if (!Strings.isNullOrEmpty(this.command.getWillRemoveIndexes())) {
            Arrays.asList(this.command.getWillRemoveIndexes().split(",")).forEach((i) -> {
                LOG.info("{} index is unregistering...");
                rafService.unregisterIndexes(i);
                LOG.info("{} index is unregistered...");
            });
        }
    }

}
