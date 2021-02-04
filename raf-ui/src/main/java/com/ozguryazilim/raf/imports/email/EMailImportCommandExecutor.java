package com.ozguryazilim.raf.imports.email;

import com.ozguryazilim.telve.messagebus.command.AbstractCommandExecuter;
import com.ozguryazilim.telve.messagebus.command.CommandExecutor;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author oyas
 */
@CommandExecutor(command = EMailImportCommand.class)
public class EMailImportCommandExecutor extends AbstractCommandExecuter<EMailImportCommand> {

    private static final Logger LOG = LoggerFactory.getLogger(EMailImportCommandExecutor.class);

    @Inject
    private RafEMailImporter importer;

    @Override
    public void execute(EMailImportCommand command) {

        LOG.debug("EMail RAF Import Start");

        importer.importMail(command);
    }

}
