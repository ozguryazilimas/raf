package com.ozguryazilim.raf.command.reindexmanagercommand;

import com.ozguryazilim.telve.messagebus.command.AbstractStorableCommand;

public class ReindexManagerCommand extends AbstractStorableCommand {

    private String path;
    private boolean isAsync = Boolean.TRUE;

    public ReindexManagerCommand(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isAsync() {
        return isAsync;
    }

    public void setAsync(boolean async) {
        isAsync = async;
    }

}
