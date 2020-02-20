package com.ozguryazilim.raf.folderdivcommand;

import com.ozguryazilim.raf.imports.*;
import com.ozguryazilim.telve.messagebus.command.ui.CommandEditor;
import com.ozguryazilim.telve.messagebus.command.ui.CommandEditorBase;

@CommandEditor(command = FolderDivideByYearsCommand.class, page = RafCommandPages.FolderDivideByYearsCommandEditor.class)
public class FolderDivideByYearsCommandEditor extends CommandEditorBase<FolderDivideByYearsCommand> {

    @Override
    public FolderDivideByYearsCommand createNewCommand() {
        return new FolderDivideByYearsCommand("", Boolean.TRUE, Boolean.FALSE);
    }

}
