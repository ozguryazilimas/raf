package com.ozguryazilim.raf.imports;

import com.ozguryazilim.telve.messagebus.command.ui.CommandEditor;
import com.ozguryazilim.telve.messagebus.command.ui.CommandEditorBase;
import org.apache.commons.lang3.StringUtils;

@CommandEditor(command = FileImporterCommand.class, page = RafCommandPages.FileImporterCommandEditor.class)
public class FileImporterCommandEditor extends CommandEditorBase<FileImporterCommand> {

    @Override
    public FileImporterCommand createNewCommand() {
        return new FileImporterCommand(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY);
    }

}
