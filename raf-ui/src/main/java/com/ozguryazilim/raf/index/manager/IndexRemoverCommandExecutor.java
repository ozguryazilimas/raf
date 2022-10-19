package com.ozguryazilim.raf.index.manager;

import com.google.common.base.Strings;
import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.telve.messagebus.command.AbstractCommandExecuter;
import com.ozguryazilim.telve.messagebus.command.CommandExecutor;
import java.util.Arrays;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            String[] indexNames = this.command.getWillRemoveIndexes().split(",");
            LOG.info("%s indexes are unregistering...", Arrays.toString(indexNames));
            try {
                rafService.unregisterIndexes(indexNames);
            } catch (RafException ex) {
                LOG.error(String.format("Error while unregistering indexes %s", Arrays.toString(indexNames)), ex);
            }
        }
    }

}
