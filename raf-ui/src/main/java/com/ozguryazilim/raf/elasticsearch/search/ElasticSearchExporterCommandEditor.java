package com.ozguryazilim.raf.elasticsearch.search;

import com.ozguryazilim.raf.imports.*;
import com.ozguryazilim.telve.messagebus.command.ui.CommandEditor;
import com.ozguryazilim.telve.messagebus.command.ui.CommandEditorBase;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

@CommandEditor(command = ElasticSearchExporterCommand.class, page = RafCommandPages.ElasticSearchExporterCommandEditor.class)
public class ElasticSearchExporterCommandEditor extends CommandEditorBase<ElasticSearchExporterCommand> {

    @Override
    public ElasticSearchExporterCommand createNewCommand() {
        return new ElasticSearchExporterCommand("localhost", 9200, "raf_repository", StringUtils.EMPTY, StringUtils.EMPTY, Boolean.FALSE, Boolean.FALSE, new Date());
    }

}
