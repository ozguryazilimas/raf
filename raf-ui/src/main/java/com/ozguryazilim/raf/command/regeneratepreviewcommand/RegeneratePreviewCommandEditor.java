package com.ozguryazilim.raf.command.regeneratepreviewcommand;

import com.ozguryazilim.raf.imports.*;
import com.ozguryazilim.raf.models.RafFolder;
import com.ozguryazilim.telve.lookup.LookupSelectTuple;
import com.ozguryazilim.telve.messagebus.command.ui.CommandEditor;
import com.ozguryazilim.telve.messagebus.command.ui.CommandEditorBase;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.SelectEvent;

@CommandEditor(command = RegeneratePreviewCommand.class, page = RafCommandPages.RegeneratePreviewCommandEditor.class)
public class RegeneratePreviewCommandEditor extends CommandEditorBase<RegeneratePreviewCommand> {

    @Override
    public RegeneratePreviewCommand createNewCommand() {
        return new RegeneratePreviewCommand(StringUtils.EMPTY);
    }

    public void selectFolder(SelectEvent event) {
        if (event != null) {
            LookupSelectTuple sl = (LookupSelectTuple) event.getObject();
            if (sl != null) {
                this.getCommand().setRafPath(((RafFolder) sl.getValue()).getPath());
            }
        }
    }

}
