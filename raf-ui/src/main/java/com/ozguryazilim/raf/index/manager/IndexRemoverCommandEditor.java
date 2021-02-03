package com.ozguryazilim.raf.index.manager;

import com.ozguryazilim.raf.imports.*;
import com.ozguryazilim.telve.messagebus.command.ui.CommandEditor;
import com.ozguryazilim.telve.messagebus.command.ui.CommandEditorBase;

@CommandEditor(command = IndexRemoverCommand.class, page = RafCommandPages.IndexRemoverCommandEditor.class)
public class IndexRemoverCommandEditor extends CommandEditorBase<IndexRemoverCommand> {

    @Override
    public IndexRemoverCommand createNewCommand() {
        return new IndexRemoverCommand("");
    }

}
