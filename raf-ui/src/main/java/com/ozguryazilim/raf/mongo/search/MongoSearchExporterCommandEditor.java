package com.ozguryazilim.raf.mongo.search;

import com.ozguryazilim.raf.imports.*;
import com.ozguryazilim.telve.messagebus.command.ui.CommandEditor;
import com.ozguryazilim.telve.messagebus.command.ui.CommandEditorBase;
import org.apache.commons.lang3.StringUtils;

@CommandEditor(command = MongoSearchExporterCommand.class, page = RafCommandPages.MongoSearchExporterCommandEditor.class)
public class MongoSearchExporterCommandEditor extends CommandEditorBase<MongoSearchExporterCommand> {

    @Override
    public MongoSearchExporterCommand createNewCommand() {
        return new MongoSearchExporterCommand("localhost", 27017, "raf_repository", StringUtils.EMPTY, StringUtils.EMPTY, Boolean.FALSE);
    }

}
