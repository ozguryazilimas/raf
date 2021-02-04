package com.ozguryazilim.raf.externaldoc.importer;

import com.ozguryazilim.raf.externaldoc.config.ExternalDocDoxoftImporterCommandPages;
import com.ozguryazilim.telve.messagebus.command.ui.CommandEditor;
import com.ozguryazilim.telve.messagebus.command.ui.CommandEditorBase;
import org.apache.commons.lang3.StringUtils;

@CommandEditor(command = DoxoftImporterCommand.class, page = ExternalDocDoxoftImporterCommandPages.DoxoftImporterCommandEditor.class)
public class DoxoftImporterCommandEditor extends CommandEditorBase<DoxoftImporterCommand> {

    @Override
    public DoxoftImporterCommand createNewCommand() {
        return new DoxoftImporterCommand(StringUtils.EMPTY, 30306, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, Boolean.FALSE, Boolean.FALSE);
    }

}
