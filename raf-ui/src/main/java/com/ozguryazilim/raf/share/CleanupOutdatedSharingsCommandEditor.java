package com.ozguryazilim.raf.share;

import com.ozguryazilim.raf.imports.RafCommandPages;
import com.ozguryazilim.telve.messagebus.command.ui.CommandEditor;
import com.ozguryazilim.telve.messagebus.command.ui.CommandEditorBase;

@CommandEditor(command = CleanupOutdatedSharingsCommand.class, page = RafCommandPages.CleanupOutdatedSharingsCommandEditor.class)
public class CleanupOutdatedSharingsCommandEditor extends CommandEditorBase<CleanupOutdatedSharingsCommand> {

    @Override
    public CleanupOutdatedSharingsCommand createNewCommand() {
        return new CleanupOutdatedSharingsCommand();
    }

}