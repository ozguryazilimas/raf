package com.ozguryazilim.raf.command.cleanupexpireduploadscommand;

import com.ozguryazilim.raf.imports.RafCommandPages;
import com.ozguryazilim.telve.messagebus.command.ui.CommandEditor;
import com.ozguryazilim.telve.messagebus.command.ui.CommandEditorBase;

@CommandEditor(command = CleanupExpiredUploadsCommand.class, page = RafCommandPages.CleanupExpiredUploadsCommandEditor.class)
public class CleanupExpiredUploadsCommandEditor extends CommandEditorBase<CleanupExpiredUploadsCommand> {

    @Override
    public CleanupExpiredUploadsCommand createNewCommand() {
        return new CleanupExpiredUploadsCommand();
    }

}
