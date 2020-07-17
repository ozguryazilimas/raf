package com.ozguryazilim.raf.imports.email;

import com.ozguryazilim.raf.imports.RafCommandPages;
import com.ozguryazilim.telve.messagebus.command.ui.CommandEditor;
import com.ozguryazilim.telve.messagebus.command.ui.CommandEditorBase;

/**
 *
 * @author oyas
 */
@CommandEditor(command = EMailFetchCommand.class, page = RafCommandPages.EMailFetchCommandEditor.class)
public class EMailFetchCommandEditor extends CommandEditorBase<EMailFetchCommand> {

    @Override
    public EMailFetchCommand createNewCommand() {
        return new EMailFetchCommand("imap", "", 993, "", "", Boolean.TRUE, "INBOX", "archive", "", "telve");
    }

}
