package com.ozguryazilim.raf.regeneratepreviewcommand;

import com.ozguryazilim.telve.messagebus.command.AbstractStorableCommand;

public class RegeneratePreviewCommand extends AbstractStorableCommand {

    private String rafPath;
    private boolean regenerateOnlyMissingPreviews;

    public RegeneratePreviewCommand(String rafPath) {
        this.rafPath = rafPath;
    }

    public String getRafPath() {
        return rafPath;
    }

    public void setRafPath(String rafPath) {
        this.rafPath = rafPath;
    }

    public boolean isRegenerateOnlyMissingPreviews() {
        return regenerateOnlyMissingPreviews;
    }

    public void setRegenerateOnlyMissingPreviews(boolean regenerateOnlyMissingPreviews) {
        this.regenerateOnlyMissingPreviews = regenerateOnlyMissingPreviews;
    }
}
