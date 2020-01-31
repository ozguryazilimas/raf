package com.ozguryazilim.raf.imports;

import com.ozguryazilim.telve.messagebus.command.ui.CommandEditor;
import com.ozguryazilim.telve.messagebus.command.ui.CommandEditorBase;
import org.apache.commons.lang3.StringUtils;

@CommandEditor(command = DoxoftImporterCommand.class, page = RafCommandPages.DoxoftImporterCommandEditor.class)
public class DoxoftImporterCommandEditor extends CommandEditorBase<DoxoftImporterCommand> {

    @Override
    public DoxoftImporterCommand createNewCommand() {
        return new DoxoftImporterCommand(StringUtils.EMPTY, 30306, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, Boolean.FALSE, Boolean.FALSE);
    }

}
