package com.ozguryazilim.raf.regeneratepreviewcommand;

import com.ozguryazilim.telve.messagebus.command.AbstractStorableCommand;

public class RegeneratePreviewCommand extends AbstractStorableCommand {

    private String rafPath;

    public RegeneratePreviewCommand(String rafPath) {
        this.rafPath = rafPath;
    }

    public String getRafPath() {
        return rafPath;
    }

    public void setRafPath(String rafPath) {
        this.rafPath = rafPath;
    }

}
