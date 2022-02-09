package com.ozguryazilim.raf.command.checkmissingcontentscommand;

import com.ozguryazilim.raf.imports.RafCommandPages;
import com.ozguryazilim.telve.messagebus.command.ui.CommandEditor;
import com.ozguryazilim.telve.messagebus.command.ui.CommandEditorBase;

@CommandEditor(command = CheckMissingContentsCommand.class, page = RafCommandPages.CleanupExpiredUploadsCommandEditor.class)
public class CheckMissingContentsCommandEditor extends CommandEditorBase<CheckMissingContentsCommand> {

    @Override
    public CheckMissingContentsCommand createNewCommand() {
        return new CheckMissingContentsCommand();
    }

}
