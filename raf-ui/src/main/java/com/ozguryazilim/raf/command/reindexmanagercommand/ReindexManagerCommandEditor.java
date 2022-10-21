package com.ozguryazilim.raf.command.reindexmanagercommand;

import com.ozguryazilim.raf.RafException;
import com.ozguryazilim.raf.RafService;
import com.ozguryazilim.raf.imports.RafCommandPages;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.telve.lookup.LookupSelectTuple;
import com.ozguryazilim.telve.messagebus.command.ui.CommandEditor;
import com.ozguryazilim.telve.messagebus.command.ui.CommandEditorBase;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.SelectEvent;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@CommandEditor(command = ReindexManagerCommand.class, page = RafCommandPages.ReindexManagerCommandEditor.class)
public class ReindexManagerCommandEditor extends CommandEditorBase<ReindexManagerCommand> {

    @Inject
    RafService rafService;

    @Override
    public ReindexManagerCommand createNewCommand() {
        return new ReindexManagerCommand(StringUtils.EMPTY);
    }

    public Map<String, Boolean> getIndexStatus() throws RafException {
        return rafService.getIndexDefinitions().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().isEnabled()));
    }

}
